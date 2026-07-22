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
import com.cowave.zoo.framework.access.filter.AccessIdGenerator;
import com.cowave.zoo.framework.helper.redis.RedisHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
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
    private final RedisHelper redisHelper;
    private final ObjectMapper objectMapper;
    private final AccessIdGenerator accessIdGenerator;
    private final BearerTokenDelegate bearerTokenDelegate;

    @Override
    public void assignAccessToken(AccessUserDetails userDetails) {
        doAssignAccessToken(userDetails, false);
    }

    public void doAssignAccessToken(AccessUserDetails userDetails, boolean useRefreshToken) {
        JwtBuilder jwtBuilder = Jwts.builder();
        // 构造accessToken
        bearerTokenDelegate.setAccessClaims(jwtBuilder, userDetails);
        String issuer = bearerTokenDelegate.getAccessIssuer();
        int accessExpire = bearerTokenDelegate.getAccessExpireSeconds();
        SignatureAlgorithm algorithm = bearerTokenDelegate.getAccessAlgorithm();
        Key signingKey = bearerTokenDelegate.getAccessSigningKey(algorithm);
        String accessToken = jwtBuilder
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .signWith(algorithm, signingKey)
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire * 1000L))
                .compact();
        // 填充userDetails
        userDetails.setAccessToken(accessToken);
        // 保存到上下文中
        Access access = Access.get();
        if (access == null) {
            access = Access.newAccess(accessIdGenerator);
        }
        access.setUserDetails(userDetails);
        Access.set(access);
        // 尝试设置Cookie
        if ("cookie".equals(bearerTokenDelegate.tokenStore())) {
            Access.setCookie(bearerTokenDelegate.tokenKey(), accessToken, "/", accessExpire);
        }
        // 服务端保存
        if (userDetails.isAccessValid() && redisHelper != null) {
            // 仅使用accessToken且不允许同时登录，那么删掉其它令牌
            if(userDetails.isAccessUnique() && !useRefreshToken){
                String tenantId = userDetails.getTenantId();
                String authType = userDetails.getAuthType();
                String userAccount = userDetails.getUsername();
                redisHelper.deleteByPattern(bearerTokenDelegate.getAccessIssuer()
                        + ":auth:" + tenantId + ":access:" + authType + ":" + userAccount + ":*");
            }
            // 记录本次发放的令牌
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(userDetails);
            redisHelper.putExpire(getAccessTokenKey(userDetails), accessTokenInfo, accessExpire, TimeUnit.SECONDS);
        }
    }

    @Override
    public void assignAccessRefreshToken(AccessUserDetails userDetails) {
        doAssignAccessToken(userDetails, true);
        assignRefreshToken(userDetails);
    }

    private void assignRefreshToken(AccessUserDetails userDetails) {
        JwtBuilder jwtBuilder = Jwts.builder();
        // 构造refreshToken
        bearerTokenDelegate.setRefreshClaims(jwtBuilder, userDetails);
        String issuer = bearerTokenDelegate.getRefreshIssuer();
        SignatureAlgorithm algorithm = bearerTokenDelegate.getRefreshAlgorithm();
        Key signingKey = bearerTokenDelegate.getRefreshSigningKey(algorithm);
        String refreshToken = jwtBuilder
                .setIssuer(issuer)
                .setIssuedAt(new Date())
                .signWith(algorithm, signingKey)
                .compact();
        userDetails.setRefreshToken(refreshToken);
        // 服务端保存
        if (redisHelper != null) {
            int refreshExpire = bearerTokenDelegate.getRefreshExpireSeconds();
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(userDetails);
            redisHelper.putExpire(getRefreshTokenKey(userDetails), refreshTokenInfo, refreshExpire, TimeUnit.SECONDS);
        }
    }

    @Override
    public void assignOauthToken(AccessUserDetails userDetails) {
        // 构造accessToken
        JwtBuilder oauthAccessBuilder = Jwts.builder();
        bearerTokenDelegate.setOauthAccessClaims(oauthAccessBuilder, userDetails);
        String accessIssuer = bearerTokenDelegate.getAccessIssuer();
        int accessExpire = bearerTokenDelegate.getAccessExpireSeconds();
        SignatureAlgorithm accessAlgorithm = bearerTokenDelegate.getAccessAlgorithm();
        Key accessSigningKey = bearerTokenDelegate.getAccessSigningKey(accessAlgorithm);
        String oauthAccess = oauthAccessBuilder
                .setIssuer(accessIssuer)
                .setIssuedAt(new Date())
                .signWith(accessAlgorithm, accessSigningKey)
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire * 1000L))
                .compact();
        userDetails.setAccessToken(oauthAccess);
        // 构造refreshToken
        JwtBuilder oauthRefreshBuilder = Jwts.builder();
        bearerTokenDelegate.setOauthRefreshClaims(oauthRefreshBuilder, userDetails);
        String refreshIssuer = bearerTokenDelegate.getRefreshIssuer();
        SignatureAlgorithm refreshAlgorithm = bearerTokenDelegate.getRefreshAlgorithm();
        Key refreshSigningKey = bearerTokenDelegate.getRefreshSigningKey(refreshAlgorithm);
        String oauthRefreshToken = oauthRefreshBuilder
                .setIssuer(refreshIssuer)
                .setIssuedAt(new Date())
                .signWith(refreshAlgorithm, refreshSigningKey)
                .compact();
        userDetails.setRefreshToken(oauthRefreshToken);
        // 服务端保存
        if (redisHelper != null) {
            int refreshExpire = bearerTokenDelegate.getRefreshExpireSeconds();
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(userDetails);
            String oauthKey = getOauthTokenKey(userDetails.getTenantId(),
                    userDetails.getAuthType(), userDetails.getUsername(), userDetails.getOauthId());
            redisHelper.putExpire(oauthKey, refreshTokenInfo, refreshExpire, TimeUnit.SECONDS);
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
        assert redisHelper != null;
        Claims claims;
        try {
            SignatureAlgorithm algorithm = bearerTokenDelegate.getRefreshAlgorithm();
            Key verificationKey = bearerTokenDelegate.getRefreshVerificationKey(algorithm);
            claims = Jwts.parser().setSigningKey(verificationKey).parseClaimsJws(refreshToken).getBody();
        } catch (Exception e) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.invalid}");
        }

        AccessUserDetails details = bearerTokenDelegate.parseRefreshClaims(claims);
        // 获取服务保存的Token
        String refreshTokenKey = getRefreshTokenKey(details.getTenantId(), details.getAuthType(), details.getUsername());
        RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(refreshTokenKey);
        if (refreshTokenInfo == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 比对id，判断Token是否已经被刷新过
        if (details.isAccessUnique() && !Objects.equals(details.getRefreshId(), refreshTokenInfo.getRefreshId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }

        //当前accessToken删除
        String accessId = refreshTokenInfo.getAccessId();
        if (details.isAccessValid()) {
            String accessTokenKey = getAccessTokenKey(details.getTenantId(), details.getAuthType(), details.getUsername(), accessId);
            redisHelper.delete(accessTokenKey);
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
        assert redisHelper != null;
        Claims claims;
        try {
            SignatureAlgorithm algorithm = bearerTokenDelegate.getRefreshAlgorithm();
            Key verificationKey = bearerTokenDelegate.getRefreshVerificationKey(algorithm);
            claims = Jwts.parser().setSigningKey(verificationKey).parseClaimsJws(oauthToken).getBody();
        } catch (Exception e) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.invalid}");
        }

        AccessUserDetails details = bearerTokenDelegate.parseOauthRefreshClaims(claims);
        // 获取服务保存的Token
        String oauthTokenKey = getOauthTokenKey(
                details.getTenantId(), details.getAuthType(), details.getUsername(), details.getOauthId());
        RefreshTokenInfo oauthTokenInfo = redisHelper.getValue(oauthTokenKey);
        if (oauthTokenInfo == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 比对id，判断Token是否已经被刷新过
        if (details.isAccessUnique() && !Objects.equals(details.getRefreshId(), oauthTokenInfo.getRefreshId())) {
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
        if ("cookie".equals(bearerTokenDelegate.tokenStore())) {
            authorization = Access.getCookie(bearerTokenDelegate.tokenKey());
        } else {
            authorization = Access.getRequestHeader(bearerTokenDelegate.tokenKey());
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
            SignatureAlgorithm algorithm = bearerTokenDelegate.getAccessAlgorithm();
            Key verificationKey = bearerTokenDelegate.getAccessVerificationKey(algorithm);
            claims = Jwts.parser().setSigningKey(verificationKey).parseClaimsJws(accessToken).getBody();
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
        AccessUserDetails userDetails = bearerTokenDelegate.parseAccessClaims(claims);
        if (useRefreshToken) {
            // IP变化，要求重新刷一下accessToken
            if (userDetails.isAccessUnique() && !Objects.equals(Access.accessIp(), userDetails.getAccessIp())) {
                writeResponse(response, INVALID_TOKEN, "frame.auth.access.changed.ip");
                return null;
            }
        }
        // OAuth令牌访问应用限制
        String oauthAppId = bearerTokenDelegate.oauthAppId();
        if(StringUtils.isNotBlank(oauthAppId) && !oauthAppId.equals(userDetails.getOauthId())){
            if (response == null) {
                throw new HttpHintException(UNAUTHORIZED, "{frame.oauth.invalid}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.oauth.invalid");
            return null;
        }
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
            SignatureAlgorithm algorithm = bearerTokenDelegate.getAccessAlgorithm();
            Key verificationKey = bearerTokenDelegate.getAccessVerificationKey(algorithm);
            claims = Jwts.parser().setSigningKey(verificationKey).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            writeResponse(response, INVALID_TOKEN, "frame.auth.access.expire");
            return null;
        } catch (Exception e) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.access.invalid");
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

        if (ACCESS == bearerTokenDelegate.authMode()) {
            redisHelper.delete(getAccessTokenKey(userDetails));
        }

        if (ACCESS_REFRESH == bearerTokenDelegate.authMode()) {
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
        redisHelper.deleteByPattern(bearerTokenDelegate.getAccessIssuer()
                + ":auth:" + tenantId + ":access:" + authType + ":" + userAccount + ":*");
        redisHelper.deleteByPattern(bearerTokenDelegate.getRefreshIssuer()
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
        return redisHelper.getByPattern(bearerTokenDelegate.getAccessIssuer() + ":auth:" + tenantId + ":access:*");
    }

    @Override
    public List<RefreshTokenInfo> listRefreshToken(String tenantId) {
        return redisHelper.getByPattern(bearerTokenDelegate.getRefreshIssuer() + ":auth:" + tenantId + ":refresh:*");
    }

    @Override
    public List<RefreshTokenInfo> listOauthToken(String tenantId) {
        return redisHelper.getByPattern(bearerTokenDelegate.getRefreshIssuer() + ":auth:" + tenantId + ":oauth:*");
    }

    private String getAccessTokenKey(AccessUserDetails userDetails) {
        return getAccessTokenKey(userDetails.getTenantId(),
                userDetails.getAuthType(), userDetails.getUsername(), userDetails.getAccessId());
    }

    private String getAccessTokenKey(String tenantId, String type, String userAccount, String accessId) {
        return AUTH_ACCESS_KEY.formatted(bearerTokenDelegate.getAccessIssuer(), tenantId, type, userAccount, accessId);
    }

    private String getRefreshTokenKey(AccessUserDetails userDetails) {
        return getRefreshTokenKey(userDetails.getTenantId(), userDetails.getAuthType(), userDetails.getUsername());
    }

    private String getRefreshTokenKey(String tenantId, String type, String userAccount) {
        return AUTH_REFRESH_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(), tenantId, type, userAccount);
    }

    private String getOauthTokenKey(String tenantId, String type, String userAccount, String appId) {
        return AUTH_OAUTH_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(), tenantId, type, userAccount, appId);
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
            SignatureAlgorithm algorithm = bearerTokenDelegate.getAccessAlgorithm();
            Key verificationKey = bearerTokenDelegate.getAccessVerificationKey(algorithm);
            Jwts.parser().setSigningKey(verificationKey).parseClaimsJws(accessToken).getBody();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void writeResponse(HttpServletResponse response, ResponseCode responseCode, String messageKey) throws IOException {
        int httpStatus = responseCode.getStatus();
        if (bearerTokenDelegate.alwaysReturnHttp200()) {
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
