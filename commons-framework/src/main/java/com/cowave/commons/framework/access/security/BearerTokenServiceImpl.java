/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.security;

import cn.hutool.core.util.IdUtil;
import com.cowave.commons.client.http.asserts.HttpHintException;
import com.cowave.commons.client.http.asserts.I18Messages;
import com.cowave.commons.client.http.response.Response;
import com.cowave.commons.client.http.response.ResponseCode;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessProperties;
import com.cowave.commons.framework.access.filter.AccessIdGenerator;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.redis.RedisHelper;
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

import static com.cowave.commons.client.http.constants.HttpCode.*;

/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
public class BearerTokenServiceImpl implements BearerTokenService {
    public static final String AUTH_ACCESS_KEY = "%s:auth:%s:access:%s";
    public static final String AUTH_REFRESH_KEY = "%s:auth:%s:refresh:%s:%s";
    private final ApplicationProperties applicationProperties;
    private final AccessProperties accessProperties;
    private final AccessIdGenerator accessIdGenerator;
    private final ObjectMapper objectMapper;
    private final RedisHelper redisHelper;
    private final BearerTokenInterceptor bearerTokenInterceptor;

    @Override
    public void assignAccessToken(AccessUserDetails userDetails) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .claim(CLAIM_TYPE, userDetails.getAuthType())
                .claim(CLAIM_ACCESS_IP, Access.accessIp())
                .claim(CLAIM_ACCESS_ID, userDetails.getAccessId())
                .claim(CLAIM_CONFLICT, userDetails.isConflict() ? "Y" : "N")
                .claim(CLAIM_TENANT_ID, userDetails.getTenantId())
                .claim(CLAIM_USER_ID, userDetails.getUserId())
                .claim(CLAIM_USER_CODE, userDetails.getUserCode())
                .claim(CLAIM_USER_PROPERTIES, userDetails.getUserProperties())
                .claim(CLAIM_USER_TYPE, userDetails.getUserType())
                .claim(CLAIM_USER_NAME, userDetails.getUserNick())
                .claim(CLAIM_USER_ACCOUNT, userDetails.getUsername())
                .claim(CLAIM_USER_ROLE, userDetails.getRoles())
                .claim(CLAIM_USER_PERM, userDetails.getPermissions())
                .claim(CLAIM_DEPT_ID, userDetails.getDeptId())
                .claim(CLAIM_DEPT_CODE, userDetails.getDeptCode())
                .claim(CLAIM_DEPT_NAME, userDetails.getDeptName())
                .claim(CLAIM_CLUSTER_ID, userDetails.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, userDetails.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME, userDetails.getClusterName());

        if(bearerTokenInterceptor != null){
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
        if(accessProperties.accessStore() && redisHelper != null){
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(userDetails);
            redisHelper.putExpire(getAccessTokenKey(userDetails.getTenantId(), userDetails.getAccessId()),
                    accessTokenInfo, accessProperties.accessExpire(), TimeUnit.SECONDS);
        }
    }

    @Override
    public void assignAccessRefreshToken(AccessUserDetails userDetails) {
        assignAccessToken(userDetails);
        assignRefreshToken(userDetails);
    }

    private void assignRefreshToken(AccessUserDetails userDetails) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .claim(CLAIM_TYPE, userDetails.getAuthType())
                .claim(CLAIM_REFRESH_ID, userDetails.getRefreshId())
                .claim(CLAIM_USER_ACCOUNT, userDetails.getUsername())
                .claim(CLAIM_TENANT_ID, userDetails.getTenantId())
                .claim(CLAIM_CONFLICT, userDetails.isConflict() ? "Y" : "N");

        if(bearerTokenInterceptor != null){
            bearerTokenInterceptor.additionalRefreshClaims(jwtBuilder);
        }
        String refreshToken = jwtBuilder
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.refreshSecret())
                .compact();
        userDetails.setRefreshToken(refreshToken);
        // 服务端保存
        if(redisHelper != null){
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(userDetails);
            redisHelper.putExpire(getRefreshTokenKey(userDetails.getTenantId(), userDetails.getAuthType(), userDetails.getUsername()),
                    refreshTokenInfo, accessProperties.refreshExpire(), TimeUnit.SECONDS);
        }
    }

    @Override
    public String refreshAccessToken() throws Exception {
        AccessUserDetails userDetails = parseAccessToken(null);
        if(redisHelper != null){
            AccessTokenInfo accessTokenInfo =
                    redisHelper.getValue(getAccessTokenKey(userDetails.getTenantId(), userDetails.getAccessId()));
            if(accessTokenInfo != null){
                userDetails.setLoginIp(accessTokenInfo.getLoginIp());
                userDetails.setLoginTime(accessTokenInfo.getLoginTime());
                userDetails.setClusterName(accessTokenInfo.getAccessCluster());
                redisHelper.delete(getAccessTokenKey(userDetails.getTenantId(), userDetails.getAccessId()));
            }
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
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.invalid}");
        }

        String tenantId = (String) claims.get(CLAIM_TENANT_ID);
        String type = (String) claims.get(CLAIM_TYPE);
        String userAccount = (String) claims.get(CLAIM_USER_ACCOUNT);
        String refreshId = (String) claims.get(CLAIM_REFRESH_ID);
        assert redisHelper != null;

        // 获取服务保存的Token
        RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(getRefreshTokenKey(tenantId, type, userAccount));
        if (refreshTokenInfo == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.notexist}");
        }

        String tokenConflict = (String) claims.get(CLAIM_CONFLICT);
        // 比对id，判断Token是否已经被刷新过
        if ("Y".equals(tokenConflict) && !refreshId.equals(refreshTokenInfo.getRefreshId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.conflict}");
        }

        //当前accessToken失效
        String accessId = refreshTokenInfo.getAccessId();
        if ("Y".equals(tokenConflict) && accessProperties.accessStore()) {
            redisHelper.delete(getAccessTokenKey(tenantId, accessId));
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
    public AccessUserDetails parseAccessToken(HttpServletResponse response) throws IOException {
        String accessToken = getAccessToken();
        if (accessToken != null) {
            return parseAccessToken(accessToken, response);
        }
        if (response == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.no}");
        }
        writeResponse(response, UNAUTHORIZED, "frame.auth.no");
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
                throw new HttpHintException(UNAUTHORIZED, "{frame.auth.expired}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.auth.expired");
            return null;
        } catch (Exception e) {
            if (response == null) {
                throw new HttpHintException(UNAUTHORIZED, "{frame.auth.invalid}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.auth.invalid");
            return null;
        }
        return doParseAccessToken(claims, response);
    }

    private AccessUserDetails doParseAccessToken(Claims claims, HttpServletResponse response) throws IOException {
        String accessId = (String) claims.get(CLAIM_ACCESS_ID);
        String tenantId = (String) claims.get(CLAIM_TENANT_ID);

        AccessUserDetails userDetails = new AccessUserDetails();
        userDetails.setAuthType((String) claims.get(CLAIM_TYPE));
        userDetails.setAccessId(accessId);
        userDetails.setRefreshId((String) claims.get(CLAIM_REFRESH_ID));
        userDetails.setTenantId(tenantId);
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
        // 服务端校验
        if(accessProperties.accessCheck() && redisHelper != null
                && !redisHelper.existKey(getAccessTokenKey(tenantId, accessId))){
            if (response == null) {
                throw new HttpHintException(UNAUTHORIZED, "{frame.auth.denied}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.auth.denied");
        }

        // 处理自定义校验
        if(bearerTokenInterceptor != null){
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
        writeResponse(response, UNAUTHORIZED, "frame.auth.no");
        return null;
    }

    private AccessUserDetails parseAccessRefreshToken(String accessToken, HttpServletResponse response) throws IOException {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(accessProperties.accessSecret()).parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            writeResponse(response, INVALID_TOKEN, "frame.auth.expired");
            return null;
        } catch (Exception e) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.invalid");
            return null;
        }

        // IP变化，要求重新刷一下accessToken
        String accessIp = (String) claims.get(CLAIM_ACCESS_IP);
        String tokenConflict = (String) claims.get(CLAIM_CONFLICT);
        if ("Y".equals(tokenConflict) && !Objects.equals(Access.accessIp(), accessIp)) {
            writeResponse(response, INVALID_TOKEN, "frame.auth.ipchanged");
            return null;
        }
        return doParseAccessToken(claims, response);
    }

    @Override
    public AccessTokenInfo revokeAccessToken(String tenantId, String accessId) {
        String accesskey = getAccessTokenKey(tenantId, accessId);
        AccessTokenInfo accessTokenInfo = redisHelper.getValue(accesskey);
        redisHelper.delete(accesskey);
        return accessTokenInfo;
    }

    @Override
    public RefreshTokenInfo revokeAccessRefreshToken() {
        AccessUserDetails userDetails = Access.userDetails();
        if(userDetails == null){
            return null;
        }

        String tenantId = userDetails.getTenantId();
        String accessId = userDetails.getAccessId();
        String userAccount = userDetails.getUsername();
        assert redisHelper != null;
        redisHelper.delete(getAccessTokenKey(tenantId, accessId));

        String refreshKey = getRefreshTokenKey(tenantId, userDetails.getAuthType(), userAccount);
        RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(refreshKey);
        redisHelper.delete(refreshKey);
        return refreshTokenInfo;
    }

    @Override
    public List<AccessTokenInfo> listAccessToken(String tenantId, String userAccount, Date beginTime, Date endTime) {
        if(StringUtils.isBlank(tenantId)){
            tenantId = "default";
        }
        List<AccessTokenInfo> list = new ArrayList<>();
        for (String key : redisHelper.keys(applicationProperties.getName() + ":auth:" + tenantId + ":access:*")) {
            AccessTokenInfo accessTokenInfo = redisHelper.getValue(key);
            if (accessTokenInfo != null) {
                if ((userAccount != null && !accessTokenInfo.getUserAccount().contains(userAccount))
                        || (beginTime != null && beginTime.after(accessTokenInfo.getAccessTime()))
                        || (endTime != null && endTime.before(accessTokenInfo.getAccessTime()))) {
                    continue;
                }
                list.add(accessTokenInfo);
            }
        }
        return list;
    }

    private String getAccessTokenKey(String tenantId, String accessId) {
        if(StringUtils.isBlank(tenantId)){
            tenantId = "default";
        }
        return AUTH_ACCESS_KEY.formatted(applicationProperties.getName(), tenantId, accessId);
    }

    private String getRefreshTokenKey(String tenantId, String type, String userAccount) {
        if(StringUtils.isBlank(tenantId)){
            tenantId = "default";
        }
        return AUTH_REFRESH_KEY.formatted(applicationProperties.getName(), tenantId, type, userAccount);
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
