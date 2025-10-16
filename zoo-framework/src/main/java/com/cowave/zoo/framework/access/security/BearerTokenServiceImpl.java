/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.access.security;

import cn.hutool.core.util.IdUtil;
import com.cowave.zoo.http.client.asserts.HttpHintException;
import com.cowave.zoo.http.client.asserts.I18Messages;
import com.cowave.zoo.http.client.response.Response;
import com.cowave.zoo.http.client.response.ResponseCode;
import com.cowave.zoo.framework.access.Access;
import com.cowave.zoo.framework.access.AccessProperties;
import com.cowave.zoo.framework.access.filter.AccessIdGenerator;
import com.cowave.zoo.framework.configuration.ApplicationProperties;
import com.cowave.zoo.framework.helper.redis.RedisHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.cowave.zoo.framework.access.security.AuthMode.ACCESS;
import static com.cowave.zoo.http.client.constants.HttpCode.*;
import static com.cowave.zoo.framework.access.security.AuthMode.ACCESS_REFRESH;

/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
public class BearerTokenServiceImpl implements BearerTokenService {
    // {applicationName}:auth:{tenantId}:access:{type}:{userAccount}:{accessId}
    public static final String AUTH_ACCESS_KEY = "%s:auth:%s:access:%s:%s:%s";
    // {applicationName}:auth:{tenantId}:refresh:{type}:{userAccount}
    public static final String AUTH_REFRESH_KEY = "%s:auth:%s:refresh:%s:%s";
    // {applicationName}:auth:{tenantId}:oauth:{type}:{userAccount}:{appId}
    public static final String AUTH_OAUTH_KEY = "%s:auth:%s:oauth:%s:%s:%s";
    private final ApplicationProperties applicationProperties;
    private final AccessProperties accessProperties;
    private final AccessIdGenerator accessIdGenerator;
    private final ObjectMapper objectMapper;
    private final RedisHelper redisHelper;
    private final BearerTokenInterceptor bearerTokenInterceptor;

    @Override
    public void assignAccessToken(AccessUserDetails userDetails) {
        doAssignAccessToken(userDetails, false);
    }

    public void doAssignAccessToken(AccessUserDetails userDetails, boolean useRefreshToken) {
        String tenantId = userDetails.getTenantId();
        String authType = userDetails.getAuthType();
        String userAccount = userDetails.getUsername();
        JwtBuilder jwtBuilder = Jwts.builder()
                .claim(CLAIM_ACCESS_UNIQUE, userDetails.isAccessUnique() ? 1 : 0)
                .claim(CLAIM_ACCESS_VALID, userDetails.isAccessValid() ? 1 : 0)
                .claim(CLAIM_TYPE, authType)
                .claim(CLAIM_ACCESS_IP, Access.accessIp())
                .claim(CLAIM_ACCESS_ID, userDetails.getAccessId())
                .claim(CLAIM_REFRESH_ID, userDetails.getRefreshId())
                .claim(CLAIM_TENANT_ID, tenantId)
                .claim(CLAIM_USER_ID, userDetails.getUserId())
                .claim(CLAIM_USER_CODE, userDetails.getUserCode())
                .claim(CLAIM_USER_PROPERTIES, userDetails.getUserProperties())
                .claim(CLAIM_USER_TYPE, userDetails.getUserType())
                .claim(CLAIM_USER_NAME, userDetails.getUserNick())
                .claim(CLAIM_USER_ACCOUNT, userAccount)
                .claim(CLAIM_USER_ROLE, userDetails.getRoles())
                .claim(CLAIM_USER_PERM, userDetails.getPermissions())
                .claim(CLAIM_DEPT_ID, userDetails.getDeptId())
                .claim(CLAIM_DEPT_CODE, userDetails.getDeptCode())
                .claim(CLAIM_DEPT_NAME, userDetails.getDeptName())
                .claim(CLAIM_CLUSTER_ID, userDetails.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, userDetails.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME, userDetails.getClusterName());
        if (bearerTokenInterceptor != null) {
            bearerTokenInterceptor.additionalAccessClaims(jwtBuilder);
        }
        String accessToken = jwtBuilder
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.accessSecret())
                .setExpiration(new Date(System.currentTimeMillis() + accessProperties.accessExpire() * 1000L))
                .compact();
        userDetails.setAccessToken(accessToken);

        // 保存到上下文中
        Access access = Access.get();
        if (access == null) {
            access = Access.newAccess(accessIdGenerator);
        }
        access.setUserDetails(userDetails);
        Access.set(access);

        // 尝试设置Cookie
        if ("cookie".equals(accessProperties.tokenStore())) {
            Access.setCookie(accessProperties.tokenKey(), accessToken, "/", accessProperties.accessExpire());
        }

        // 服务端保存
        if (userDetails.isAccessValid() && redisHelper != null) {
            // 仅使用accessToken且不允许同时登录，那么删掉其它令牌
            if(userDetails.isAccessUnique() && !useRefreshToken){
                redisHelper.deleteByPattern(
                        applicationProperties.getName() + ":auth:" + tenantId + ":access:" + authType + ":" + userAccount + ":*");
            }

            // 记录本次发放的令牌
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(userDetails);
            redisHelper.putExpire(getAccessTokenKey(userDetails), accessTokenInfo, accessProperties.accessExpire(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void assignAccessRefreshToken(AccessUserDetails userDetails) {
        doAssignAccessToken(userDetails, true);
        assignRefreshToken(userDetails);
    }

    private void assignRefreshToken(AccessUserDetails userDetails) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .claim(CLAIM_ACCESS_UNIQUE, userDetails.isAccessUnique() ? 1 : 0)
                .claim(CLAIM_ACCESS_VALID, userDetails.isAccessValid() ? 1 : 0)
                .claim(CLAIM_TYPE, userDetails.getAuthType())
                .claim(CLAIM_REFRESH_ID, userDetails.getRefreshId())
                .claim(CLAIM_USER_ACCOUNT, userDetails.getUsername())
                .claim(CLAIM_TENANT_ID, userDetails.getTenantId());

        if (bearerTokenInterceptor != null) {
            bearerTokenInterceptor.additionalRefreshClaims(jwtBuilder);
        }
        String refreshToken = jwtBuilder
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.refreshSecret())
                .compact();
        userDetails.setRefreshToken(refreshToken);
        // 服务端保存
        if (redisHelper != null) {
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(userDetails);
            redisHelper.putExpire(getRefreshTokenKey(userDetails), refreshTokenInfo, accessProperties.refreshExpire(), TimeUnit.SECONDS);
        }
    }

    public void assignOauthToken(AccessUserDetails userDetails) {
        String tenantId = userDetails.getTenantId();
        String authType = userDetails.getAuthType();
        String userAccount = userDetails.getUsername();
        JwtBuilder oauthAccessBuilder = Jwts.builder()
                .claim(CLAIM_ACCESS_UNIQUE, userDetails.isAccessUnique() ? 1 : 0)
                .claim(CLAIM_ACCESS_VALID, userDetails.isAccessValid() ? 1 : 0)
                .claim(CLAIM_OAUTH_ID, userDetails.getOauthId())
                .claim(CLAIM_OAUTH_NAME, userDetails.getOauthName())
                .claim(CLAIM_TYPE, authType)
                .claim(CLAIM_ACCESS_IP, Access.accessIp())
                .claim(CLAIM_ACCESS_ID, userDetails.getAccessId())
                .claim(CLAIM_TENANT_ID, tenantId)
                .claim(CLAIM_USER_ID, userDetails.getUserId())
                .claim(CLAIM_USER_CODE, userDetails.getUserCode())
                .claim(CLAIM_USER_PROPERTIES, userDetails.getUserProperties())
                .claim(CLAIM_USER_TYPE, userDetails.getUserType())
                .claim(CLAIM_USER_NAME, userDetails.getUserNick())
                .claim(CLAIM_USER_ACCOUNT, userAccount)
                .claim(CLAIM_USER_ROLE, userDetails.getRoles())
                .claim(CLAIM_USER_PERM, userDetails.getPermissions())
                .claim(CLAIM_DEPT_ID, userDetails.getDeptId())
                .claim(CLAIM_DEPT_CODE, userDetails.getDeptCode())
                .claim(CLAIM_DEPT_NAME, userDetails.getDeptName())
                .claim(CLAIM_CLUSTER_ID, userDetails.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, userDetails.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME, userDetails.getClusterName());
        String oauthAccess = oauthAccessBuilder
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.accessSecret())
                .setExpiration(new Date(System.currentTimeMillis() + accessProperties.accessExpire() * 1000L))
                .compact();
        userDetails.setAccessToken(oauthAccess);

        JwtBuilder oauthRefreshBuilder = Jwts.builder()
                .claim(CLAIM_ACCESS_UNIQUE, userDetails.isAccessUnique() ? 1 : 0)
                .claim(CLAIM_ACCESS_VALID, userDetails.isAccessValid() ? 1 : 0)
                .claim(CLAIM_OAUTH_ID, userDetails.getOauthId())
                .claim(CLAIM_OAUTH_NAME, userDetails.getOauthName())
                .claim(CLAIM_TYPE, userDetails.getAuthType())
                .claim(CLAIM_REFRESH_ID, userDetails.getRefreshId())
                .claim(CLAIM_USER_ACCOUNT, userDetails.getUsername())
                .claim(CLAIM_TENANT_ID, userDetails.getTenantId());
        String oauthRefreshToken = oauthRefreshBuilder
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.refreshSecret())
                .compact();
        userDetails.setRefreshToken(oauthRefreshToken);
        // 服务端保存
        if (redisHelper != null) {
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(userDetails);
            String oauthKey = getOauthTokenKey(userDetails.getTenantId(), userDetails.getAuthType(), userDetails.getUsername(), userDetails.getOauthId());
            redisHelper.putExpire(oauthKey, refreshTokenInfo, accessProperties.refreshExpire(), TimeUnit.SECONDS);
        }
    }

    @Override
    public String refreshAccessToken() throws Exception {
        AccessUserDetails userDetails = parseAccessToken(null);
        if (userDetails.isAccessValid() && redisHelper != null) {
            redisHelper.delete(getAccessTokenKey(userDetails));
        }
        userDetails.setAccessId(IdUtil.fastSimpleUUID());
        userDetails.setAccessIp(Access.accessIp());
        userDetails.setAccessTime(Access.accessTime());

        assignAccessToken(userDetails);
        return userDetails.getAccessToken();
    }

    @Override
    public AccessUserDetails refreshAccessRefreshToken(String refreshToken) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(
                    accessProperties.refreshSecret()).parseClaimsJws(refreshToken).getBody();
        } catch (Exception e) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.invalid}");
        }

        String tenantId = (String) claims.get(CLAIM_TENANT_ID);
        String authType = (String) claims.get(CLAIM_TYPE);
        String userAccount = (String) claims.get(CLAIM_USER_ACCOUNT);
        String refreshId = (String) claims.get(CLAIM_REFRESH_ID);
        Integer accessUnique = (Integer) claims.get(CLAIM_ACCESS_UNIQUE);
        Integer accessValid = (Integer) claims.get(CLAIM_ACCESS_VALID);
        assert redisHelper != null;

        // 获取服务保存的Token
        RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(getRefreshTokenKey(tenantId, authType, userAccount));
        if (refreshTokenInfo == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 比对id，判断Token是否已经被刷新过
        if (accessUnique == 1 && !Objects.equals(refreshId, refreshTokenInfo.getRefreshId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }

        //当前accessToken删除
        String accessId = refreshTokenInfo.getAccessId();
        if (accessValid == 1) {
            redisHelper.delete(getAccessTokenKey(tenantId, authType, userAccount, accessId));
        }

        // 更新Token信息
        AccessUserDetails userDetails = new AccessUserDetails(refreshTokenInfo);
        userDetails.setAccessId(IdUtil.fastSimpleUUID());
        userDetails.setRefreshId(IdUtil.fastSimpleUUID());
        userDetails.setAccessIp(Access.accessIp());
        userDetails.setAccessTime(Access.accessTime());
        // 刷新Token并返回
        assignAccessRefreshToken(userDetails);
        return userDetails;
    }

    @Override
    public AccessUserDetails refreshOauthToken(String oauthToken) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(
                    accessProperties.refreshSecret()).parseClaimsJws(oauthToken).getBody();
        } catch (Exception e) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.invalid}");
        }

        String tenantId = (String) claims.get(CLAIM_TENANT_ID);
        String type = (String) claims.get(CLAIM_TYPE);
        String userAccount = (String) claims.get(CLAIM_USER_ACCOUNT);
        String refreshId = (String) claims.get(CLAIM_REFRESH_ID);
        Integer unique = (Integer) claims.get(CLAIM_ACCESS_UNIQUE);
        String appId = (String) claims.get(CLAIM_OAUTH_ID);
        assert redisHelper != null;

        // 获取服务保存的Token
        RefreshTokenInfo oauthTokenInfo = redisHelper.getValue(getOauthTokenKey(tenantId, type, userAccount, appId));
        if (oauthTokenInfo == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 比对id，判断Token是否已经被刷新过
        if (unique == 1 && !Objects.equals(refreshId, oauthTokenInfo.getRefreshId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }

        // 更新Token信息
        AccessUserDetails userDetails = new AccessUserDetails(oauthTokenInfo);
        userDetails.setAccessId(IdUtil.fastSimpleUUID());
        userDetails.setRefreshId(IdUtil.fastSimpleUUID());
        userDetails.setAccessIp(Access.accessIp());
        userDetails.setAccessTime(Access.accessTime());
        // 刷新Token并返回
        assignOauthToken(userDetails);
        return userDetails;
    }

    @Override
    public AccessUserDetails parseAccessToken(HttpServletResponse response) throws IOException {
        String accessToken = getAccessToken();
        if (accessToken != null) {
            return parseAccessToken(accessToken, response);
        }
        if (response == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.access.empty}");
        }
        writeResponse(response, UNAUTHORIZED, "frame.auth.access.empty");
        return null;
    }

    private String getAccessToken() {
        String authorization;
        if ("cookie".equals(accessProperties.tokenStore())) {
            authorization = Access.getCookie(accessProperties.tokenKey());
        } else {
            authorization = Access.getRequestHeader(accessProperties.tokenKey());
        }

        if (StringUtils.isEmpty(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            authorization = authorization.replace("Bearer ", "");
        }
        return authorization;
    }

    private AccessUserDetails parseAccessToken(String accessToken, HttpServletResponse response) throws IOException {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(accessProperties.accessSecret()).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            if (response == null) {
                throw new HttpHintException(UNAUTHORIZED, "{frame.auth.access.expire}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.auth.access.expire");
            return null;
        } catch (Exception e) {
            if (response == null) {
                throw new HttpHintException(UNAUTHORIZED, "{frame.auth.access.invalid}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.auth.access.invalid");
            return null;
        }
        return doParseAccessToken(claims, response, false);
    }

    private AccessUserDetails doParseAccessToken(Claims claims, HttpServletResponse response, boolean useRefreshToken) throws IOException {
        String oauthAppId = (String) claims.get(CLAIM_OAUTH_ID);
        if(StringUtils.isNotBlank(accessProperties.oauthAppId()) && !accessProperties.oauthAppId().equals(oauthAppId)){
            if (response == null) {
                throw new HttpHintException(UNAUTHORIZED, "{frame.oauth.invalid}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.oauth.invalid");
            return null;
        }

        AccessUserDetails userDetails = new AccessUserDetails();
        userDetails.setAccessUnique(1 == (Integer) claims.get(CLAIM_ACCESS_UNIQUE));
        userDetails.setAccessValid(1 == (Integer) claims.get(CLAIM_ACCESS_VALID));
        userDetails.setAuthType((String) claims.get(CLAIM_TYPE));
        userDetails.setAccessId((String) claims.get(CLAIM_ACCESS_ID));
        userDetails.setRefreshId((String) claims.get(CLAIM_REFRESH_ID));
        userDetails.setTenantId((String) claims.get(CLAIM_TENANT_ID));
        // user
        userDetails.setUserId(claims.get(CLAIM_USER_ID));
        userDetails.setUserCode(claims.get(CLAIM_USER_CODE));
        userDetails.setUsername((String) claims.get(CLAIM_USER_ACCOUNT));
        userDetails.setUserNick((String) claims.get(CLAIM_USER_NAME));
        userDetails.setUserProperties((Map<String, Object>) claims.get(CLAIM_USER_PROPERTIES));
        // dept
        userDetails.setDeptId(claims.get(CLAIM_DEPT_ID));
        userDetails.setDeptCode(claims.get(CLAIM_DEPT_CODE));
        userDetails.setDeptName((String) claims.get(CLAIM_DEPT_NAME));
        // cluster
        userDetails.setClusterId((Integer) claims.get(CLAIM_CLUSTER_ID));
        userDetails.setClusterLevel((Integer) claims.get(CLAIM_CLUSTER_LEVEL));
        userDetails.setClusterName((String) claims.get(CLAIM_CLUSTER_NAME));
        // roles
        userDetails.setRoles((List<String>) claims.get(CLAIM_USER_ROLE));
        // permits
        userDetails.setPermissions((List<String>) claims.get(CLAIM_USER_PERM));

        // 服务端校验AccessToken
        if (userDetails.isAccessValid()) {
            if (useRefreshToken) {
                RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(getRefreshTokenKey(userDetails));
                // 确认refreshTokenInfo存在
                if (refreshTokenInfo == null) {
                    writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                    return null;
                }

                // 不允许同时登录，确认refreshTokenInfo对应关系
                if (userDetails.isAccessUnique() && !userDetails.getRefreshId().equals(refreshTokenInfo.getRefreshId())) {
                    writeResponse(response, UNAUTHORIZED, "frame.auth.refresh.changed");
                    return null;
                }

                // 允许同时登录，检查是否手动标记注销
                AccessTokenInfo accessTokenInfo = redisHelper.getValue(getAccessTokenKey(userDetails));
                if(accessTokenInfo == null || accessTokenInfo.getRevoked() == 1){
                    writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                    return null;
                }
            }else{
                AccessTokenInfo accessTokenInfo = redisHelper.getValue(getAccessTokenKey(userDetails));
                // 确认accessTokenInfo存在
                if (accessTokenInfo == null) {
                    writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                    return null;
                }
            }
        }

        // 处理自定义校验
        if (bearerTokenInterceptor != null) {
            bearerTokenInterceptor.additionalParseAccessToken(claims, userDetails);
        }

        // 保存到上下文中
        Access access = Access.get();
        if (access == null) {
            access = Access.newAccess(accessIdGenerator);
        }
        access.setUserDetails(userDetails);
        Access.set(access);
        return userDetails;
    }

    @Override
    public AccessUserDetails parseAccessRefreshToken(HttpServletResponse response) throws IOException {
        String accessToken = getAccessToken();
        if (accessToken != null) {
            return parseAccessRefreshToken(accessToken, response);
        }
        writeResponse(response, UNAUTHORIZED, "frame.auth.access.empty");
        return null;
    }

    private AccessUserDetails parseAccessRefreshToken(String accessToken, HttpServletResponse response) throws IOException {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(accessProperties.accessSecret()).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            writeResponse(response, INVALID_TOKEN, "frame.auth.access.expire");
            return null;
        } catch (Exception e) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.access.invalid");
            return null;
        }

        // IP变化，要求重新刷一下accessToken
        String accessIp = (String) claims.get(CLAIM_ACCESS_IP);
        Integer unique = (Integer) claims.get(CLAIM_ACCESS_UNIQUE);
        if (Objects.equals(1, unique) && !Objects.equals(Access.accessIp(), accessIp)) {
            writeResponse(response, INVALID_TOKEN, "frame.auth.access.changed.ip");
            return null;
        }
        return doParseAccessToken(claims, response, true);
    }

    @Override
    public void revoke() {
        // 服务端没有存储，不用处理
        if (redisHelper == null) {
            return;
        }

        AccessUserDetails userDetails = Access.userDetails();
        if (userDetails == null) {
            return;
        }

        if (ACCESS == accessProperties.authMode()) {
            redisHelper.delete(getAccessTokenKey(userDetails));
        }

        if (ACCESS_REFRESH == accessProperties.authMode()) {
            if (userDetails.isAccessUnique()) {
                // 不允许同时登录，就直接删除
                revokeRefreshToken(userDetails.getTenantId(), userDetails.getAuthType(), userDetails.getUsername());
            } else {
                // 允许同时登录，只将自己标记为回收
                String accessKey = getAccessTokenKey(userDetails);
                Long expireSeconds = redisHelper.getExpire(accessKey);
                if (expireSeconds > 0) {
                    AccessTokenInfo accessTokenInfo = new AccessTokenInfo(userDetails);
                    accessTokenInfo.setRevoked(1);
                    accessTokenInfo.setAccessIp(Access.accessIp());
                    accessTokenInfo.setAccessTime(Access.accessTime());
                    redisHelper.putExpire(accessKey, accessTokenInfo, expireSeconds, TimeUnit.SECONDS);
                }
            }
        }
    }

    @Override
    public AccessTokenInfo revokeAccessToken(String tenantId, String authType, String userAccount, String accessId) {
        String accesskey = getAccessTokenKey(tenantId, authType, userAccount, accessId);
        AccessTokenInfo accessTokenInfo = redisHelper.getValue(accesskey);
        redisHelper.delete(accesskey);
        return accessTokenInfo;
    }

    @Override
    public RefreshTokenInfo revokeRefreshToken(String tenantId, String authType, String userAccount) {
        String refreshKey = getRefreshTokenKey(tenantId, authType, userAccount);
        RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(refreshKey);
        redisHelper.delete(refreshKey);
        redisHelper.deleteByPattern(applicationProperties.getName()
                + ":auth:" + tenantId + ":access:" + authType + ":" + userAccount + ":*");
        redisHelper.deleteByPattern(applicationProperties.getName()
                + ":auth:" + tenantId + ":oauth:" + authType + ":" + userAccount + ":*");
        return refreshTokenInfo;
    }

    @Override
    public RefreshTokenInfo revokeOauthToken(String tenantId, String authType, String userAccount, String appId) {
        String oauthkey = getOauthTokenKey(tenantId, authType, userAccount, appId);
        RefreshTokenInfo oauthToken = redisHelper.getValue(oauthkey);
        redisHelper.delete(oauthkey);
        return oauthToken;
    }

    @Override
    public List<AccessTokenInfo> listAccessToken(String tenantId) {
        return redisHelper.getByPattern(applicationProperties.getName() + ":auth:" + tenantId + ":access:*");
    }

    @Override
    public List<RefreshTokenInfo> listRefreshToken(String tenantId) {
        return redisHelper.getByPattern(applicationProperties.getName() + ":auth:" + tenantId + ":refresh:*");
    }

    @Override
    public List<RefreshTokenInfo> listOauthToken(String tenantId) {
        return redisHelper.getByPattern(applicationProperties.getName() + ":auth:" + tenantId + ":oauth:*");
    }

    private String getAccessTokenKey(AccessUserDetails userDetails) {
        return getAccessTokenKey(userDetails.getTenantId(),
                userDetails.getAuthType(), userDetails.getUsername(), userDetails.getAccessId());
    }

    private String getAccessTokenKey(String tenantId, String type, String userAccount, String accessId) {
        return AUTH_ACCESS_KEY.formatted(applicationProperties.getName(), tenantId, type, userAccount, accessId);
    }

    private String getRefreshTokenKey(AccessUserDetails userDetails) {
        return getRefreshTokenKey(userDetails.getTenantId(), userDetails.getAuthType(), userDetails.getUsername());
    }

    private String getRefreshTokenKey(String tenantId, String type, String userAccount) {
        return AUTH_REFRESH_KEY.formatted(applicationProperties.getName(), tenantId, type, userAccount);
    }

    private String getOauthTokenKey(String tenantId, String type, String userAccount, String appId) {
        return AUTH_OAUTH_KEY.formatted(applicationProperties.getName(), tenantId, type, userAccount, appId);
    }

    @Override
    public boolean validAccessToken(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return false;
        }
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.replace("Bearer ", "");
        }
        try {
            Jwts.parser().setSigningKey(accessProperties.accessSecret()).parseClaimsJws(accessToken).getBody();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void writeResponse(HttpServletResponse response, ResponseCode responseCode, String messageKey) throws IOException {
        int httpStatus = responseCode.getStatus();
        if (accessProperties.isAlwaysSuccess()) {
            httpStatus = SUCCESS.getStatus();
        }
        response.setStatus(httpStatus);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(objectMapper.writeValueAsString(Response.msg(responseCode, I18Messages.msg(messageKey))));
        }
    }
}
