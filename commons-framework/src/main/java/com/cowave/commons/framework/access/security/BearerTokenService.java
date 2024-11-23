/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.security;

import cn.hutool.core.util.IdUtil;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessProperties;
import com.cowave.commons.framework.access.filter.AccessIdGenerator;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.redis.RedisHelper;
import com.cowave.commons.response.Response;
import com.cowave.commons.response.ResponseCode;
import com.cowave.commons.response.exception.HttpHintException;
import com.cowave.commons.response.exception.Messages;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.cowave.commons.response.HttpResponseCode.*;

/**
 *
 * @author shanhuiming
 *
 */
@SuppressWarnings("deprecation")
@ConditionalOnClass({WebSecurityConfigurerAdapter.class, Jwts.class})
@RequiredArgsConstructor
@Service
public class BearerTokenService {
    private static final String CLAIM_ID = "Token.id";
    private static final String CLAIM_TYPE = "Token.type";
    private static final String CLAIM_CONFLICT = "Token.conflict";
    private static final String CLAIM_USER_IP = "User.ip";
    private static final String CLAIM_USER_ID = "User.id";
    private static final String CLAIM_USER_CODE = "User.code";
    private static final String CLAIM_USER_NAME = "User.name";
    private static final String CLAIM_USER_ACCOUNT = "User.account";
    private static final String CLAIM_USER_ROLE = "User.role";
    private static final String CLAIM_USER_PERM = "User.permission";
    private static final String CLAIM_DEPT_ID = "Dept.id";
    private static final String CLAIM_DEPT_CODE = "Dept.code";
    private static final String CLAIM_DEPT_NAME = "Dept.name";
    private static final String CLAIM_CLUSTER_ID = "Cluster.id";
    private static final String CLAIM_CLUSTER_LEVEL = "Cluster.level";
    private static final String CLAIM_CLUSTER_NAME = "Cluster.name";

    private final AccessProperties accessProperties;

    private final ApplicationProperties applicationProperties;

    private final AccessIdGenerator accessIdGenerator;

    private final ObjectMapper objectMapper;

    @Nullable
    private final RedisHelper redisHelper;

    /**
     * 设置Token（简单模式：仅使用AccessToken）
     */
    public String simpleAssignToken(AccessToken accessToken){
        String accessJwt = Jwts.builder()
                .claim(CLAIM_USER_ID,       accessToken.getUserId())
                .claim(CLAIM_USER_CODE,     accessToken.getUserCode())
                .claim(CLAIM_USER_NAME,     accessToken.getUserNick())
                .claim(CLAIM_USER_ACCOUNT,  accessToken.getUsername())
                .claim(CLAIM_DEPT_ID,       accessToken.getDeptId())
                .claim(CLAIM_DEPT_CODE,     accessToken.getDeptCode())
                .claim(CLAIM_DEPT_NAME,     accessToken.getDeptName())
                .claim(CLAIM_CLUSTER_ID,    applicationProperties.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, applicationProperties.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME,  applicationProperties.getClusterName())
                .claim(CLAIM_USER_ROLE,     accessToken.getRoles())
                .claim(CLAIM_USER_PERM,     accessToken.getPermissions())
                .claim(CLAIM_TYPE,          accessToken.getType())
                .claim(CLAIM_USER_IP,       Access.accessIp())
                .claim(CLAIM_CONFLICT,      accessProperties.conflict() ? "Y" : "N")
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.accessSecret())
                .setExpiration(new Date(System.currentTimeMillis() + accessProperties.accessExpire() * 1000L))
                .compact();
        accessToken.setAccessToken(accessJwt);
        // 保存到上下文中
        Access access = Access.get();
        if(access == null){
            access = Access.newAccess(accessIdGenerator);
        }
        access.setAccessToken(accessToken);
        Access.set(access);

        // 尝试设置Cookie
        if("cookie".equals(accessProperties.tokenStore())){
            Access.setCookie(accessProperties.tokenKey(), accessJwt, "/", accessProperties.accessExpire());
        }
        return accessJwt;
    }

    /**
     * 解析Token（简单模式：仅使用AccessToken）
     */
    AccessToken simpleParseToken(HttpServletResponse response) throws IOException {
        String accessJwt = getAccessJwt();
        if(accessJwt != null) {
            return simpleParseAccessJwt(accessJwt, response);
        }
        if(response == null){
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.no}");
        }
        writeResponse(response, UNAUTHORIZED, "frame.auth.no");
        return null;
    }

    private String getAccessJwt() {
        String accessJwt;
        if("cookie".equals(accessProperties.tokenStore())){
            accessJwt = Access.getCookie(accessProperties.tokenKey());
        }else{
            accessJwt = Access.getRequestHeader(accessProperties.tokenKey());
        }

        if(StringUtils.isEmpty(accessJwt)) {
            return null;
        }
        if(accessJwt.startsWith("Bearer ")) {
            accessJwt = accessJwt.replace("Bearer ", "");
        }
        return accessJwt;
    }

    private AccessToken simpleParseAccessJwt(String accessJwt, HttpServletResponse response) throws IOException {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(accessProperties.accessSecret()).parseClaimsJws(accessJwt).getBody();
        } catch (Exception e) {
            if (response == null) {
                throw new HttpHintException(UNAUTHORIZED, "{frame.auth.invalid}");
            }
            writeResponse(response, UNAUTHORIZED, "frame.auth.invalid");
            return null;
        }
        return parseAccessTokenFromJwt(accessJwt, claims);
    }

    private AccessToken parseAccessTokenFromJwt(String accessJwt, Claims claims){
        AccessToken accessToken = new AccessToken();
        accessToken.setAccessToken(accessJwt);
        accessToken.setId((String)claims.get(CLAIM_ID));
        accessToken.setType((String)claims.get(CLAIM_TYPE));
        // user
        Object userId = claims.get(CLAIM_USER_ID);
        if(userId != null) {
            accessToken.setUserId(userId);
        }
        accessToken.setUserCode(claims.get(CLAIM_USER_CODE));
        accessToken.setUsername((String)claims.get(CLAIM_USER_ACCOUNT));
        accessToken.setUserNick((String)claims.get(CLAIM_USER_NAME));
        // dept
        Object deptId = claims.get(CLAIM_DEPT_ID);
        if(deptId != null) {
            accessToken.setDeptId(deptId);
        }
        accessToken.setDeptCode(claims.get(CLAIM_DEPT_CODE));
        accessToken.setDeptName((String)claims.get(CLAIM_DEPT_NAME));
        // cluster
        accessToken.setClusterId((Integer)claims.get(CLAIM_CLUSTER_ID));
        accessToken.setClusterLevel((Integer)claims.get(CLAIM_CLUSTER_LEVEL));
        accessToken.setClusterName((String)claims.get(CLAIM_CLUSTER_NAME));
        // roles
        accessToken.setRoles((List<String>)claims.get(CLAIM_USER_ROLE));
        // permits
        accessToken.setPermissions((List<String>)claims.get(CLAIM_USER_PERM));
        // 保存到上下文中
        Access access = Access.get();
        if(access == null){
            access = Access.newAccess(accessIdGenerator);
        }
        access.setAccessToken(accessToken);
        Access.set(access);
        return accessToken;
    }

    /**
     * 刷新Token（简单模式：仅使用AccessToken）
     */
    public String simpleRefreshToken() throws Exception {
        return simpleAssignToken(simpleParseToken(null));
    }

    /**
     * 设置Token（Dual模式：使用AccessToken和RefreshToken）
     */
    public void dualAssignToken(AccessToken accessToken){
        simpleAssignToken(accessToken);
        dualAssignRefreshJwt(accessToken);
    }

    private void dualAssignRefreshJwt(AccessToken accessToken) {
        accessToken.setRefreshToken(Jwts.builder()
                .claim(CLAIM_ID, accessToken.getId())
                .claim(CLAIM_TYPE, accessToken.getType())
                .claim(CLAIM_USER_ACCOUNT, accessToken.getUsername())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.refreshSecret())
                .compact());
        // 保存在服务端
        assert redisHelper != null;
        String key = applicationProperties.getTokenNamespace() + accessToken.getType() + ":" + accessToken.getUsername();
        redisHelper.putValue(key, accessToken, accessProperties.refreshExpire(), TimeUnit.SECONDS);
    }

    /**
     * 解析Token（Dual模式：使用AccessToken和RefreshToken）
     */
    AccessToken dualParseToken(HttpServletResponse response) throws IOException {
        String accessJwt = getAccessJwt();
        if(accessJwt != null) {
            return dualParseAccessJwt(accessJwt, response);
        }
        writeResponse(response, UNAUTHORIZED, "frame.auth.no");
        return null;
    }

    private AccessToken dualParseAccessJwt(String accessJwt, HttpServletResponse response) throws IOException {
        Claims claims;
        try {
            claims =  Jwts.parser().setSigningKey(
                    accessProperties.accessSecret()).parseClaimsJws(accessJwt).getBody();
        }catch(ExpiredJwtException e) {
            writeResponse(response, INVALID_TOKEN, "frame.auth.expired");
            return null;
        }catch(Exception e) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.invalid");
            return null;
        }

        // IP变化，要求重新刷一下accessToken
        String userIp = (String)claims.get(CLAIM_USER_IP);
        String tokenConflict = (String)claims.get(CLAIM_CONFLICT);
        if("Y".equals(tokenConflict) && !Objects.equals(Access.accessIp(), userIp)) {
            writeResponse(response, INVALID_TOKEN, "frame.auth.ipchanged");
            return null;
        }
        return parseAccessTokenFromJwt(accessJwt, claims);
    }

    /**
     * 刷新Token（Dual模式：使用AccessToken和RefreshToken）
     */
    public AccessToken dualRefreshToken(String refreshJwt) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(
                    accessProperties.refreshSecret()).parseClaimsJws(refreshJwt).getBody();
        } catch(Exception e) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.invalid}");
        }
        // 获取服务保存的Token
        assert redisHelper != null;
        AccessToken accessToken = redisHelper.getValue(dualRefreshKey(claims));
        if(accessToken == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.notexist}");
        }
        // 比对id，判断Token是否已经被刷新过
        String tokenId = (String)claims.get(CLAIM_ID);
        if(accessProperties.conflict() && !tokenId.equals(accessToken.getId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.conflict}");
        }
        // 更新Token信息
        accessToken.setId(IdUtil.fastSimpleUUID());
        accessToken.setAccessTime(Access.accessTime());
        accessToken.setAccessIp(Access.accessIp());
        // 刷新Token并返回
        dualAssignToken(accessToken);
        return accessToken;
    }

    /**
     * 删除Token（Dual模式：使用AccessToken和RefreshToken）
     */
    public void dualRemoveToken(HttpServletResponse response, String accessJwt) throws IOException {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(
                    accessProperties.accessSecret()).parseClaimsJws(accessJwt).getBody();
        }catch(ExpiredJwtException e) {
            claims = e.getClaims();
        }catch(Exception e) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.invalid");
            return;
        }
        assert redisHelper != null;
        redisHelper.delete(dualRefreshKey(claims));
    }

    private String dualRefreshKey(Claims claims){
        String tokenType = (String)claims.get(CLAIM_TYPE);
        String userAccount = (String)claims.get(CLAIM_USER_ACCOUNT);
        return applicationProperties.getTokenNamespace() + tokenType + ":" + userAccount;
    }

    /**
     * 创建AccessToken-Jwt（Api临时访问）
     */
    public String newApiAccessToken(AccessToken accessToken, int accessExpire) {
        return Jwts.builder()
                .claim(CLAIM_USER_ID,       accessToken.getUserId())
                .claim(CLAIM_USER_CODE,     accessToken.getUserCode())
                .claim(CLAIM_USER_NAME,     accessToken.getUserNick())
                .claim(CLAIM_USER_ACCOUNT,  accessToken.getUsername())
                .claim(CLAIM_DEPT_ID,       accessToken.getDeptId())
                .claim(CLAIM_DEPT_CODE,     accessToken.getDeptCode())
                .claim(CLAIM_DEPT_NAME,     accessToken.getDeptName())
                .claim(CLAIM_CLUSTER_ID,    applicationProperties.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, applicationProperties.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME,  applicationProperties.getClusterName())
                .claim(CLAIM_USER_ROLE,     accessToken.getRoles())
                .claim(CLAIM_USER_PERM,     accessToken.getPermissions())
                .claim(CLAIM_USER_IP,       Access.accessIp())
                .claim(CLAIM_TYPE,          AccessToken.TYPE_APP)
                .claim(CLAIM_CONFLICT,      "N")              // 不校验冲突
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessProperties.accessSecret())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire * 1000L))
                .compact();
    }

    /**
     * 验证 AccessToken-Jwt
     */
    public boolean validAccessJwt(String accessJwt) {
        if(StringUtils.isBlank(accessJwt)) {
            return false;
        }
        if(accessJwt.startsWith("Bearer ")) {
            accessJwt = accessJwt.replace("Bearer ", "");
        }
        try {
            Jwts.parser().setSigningKey(
                    accessProperties.accessSecret()).parseClaimsJws(accessJwt).getBody();
        }catch(Exception e) {
            return false;
        }
        return true;
    }

    private void writeResponse(HttpServletResponse response, ResponseCode responseCode, String messageKey) throws IOException {
        int httpStatus = responseCode.getStatus();
        if(accessProperties.isAlwaysSuccess()){
            httpStatus = SUCCESS.getStatus();
        }
        response.setStatus(httpStatus);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try(PrintWriter writer = response.getWriter()){
            writer.write(objectMapper.writeValueAsString(Response.msg(responseCode, Messages.msg(messageKey))));
        }
    }
}
