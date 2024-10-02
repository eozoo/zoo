/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.configuration.AccessConfiguration;
import com.cowave.commons.tools.Messages;
import com.cowave.commons.framework.support.redis.RedisHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Service;

import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import static org.springframework.feign.codec.ResponseCode.*;

/**
 *
 * @author shanhuiming
 *
 */
@SuppressWarnings("deprecation")
@ConditionalOnClass(WebSecurityConfigurerAdapter.class)
@RequiredArgsConstructor
@Service
public class TokenService {
    private static final String CLAIM_ID = "Token.id";
    private static final String CLAIM_TYPE = "Token.type";
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

    private final AccessConfiguration accessConfiguration;

    @Nullable
    private final RedisHelper redis;

    boolean isAlwaysSuccess(){
        return accessConfiguration.isAlwaysSuccess();
    }

    /**
     * 赋值Token
     */
    public void assignToken(AccessToken token) {
        String accessToken = Jwts.builder()
                .claim(CLAIM_USER_ID,       String.valueOf(token.getUserId())) // Long取出来是Integer，干脆用String处理
                .claim(CLAIM_USER_CODE,     token.getUserCode())
                .claim(CLAIM_USER_NAME,     token.getUserNick())
                .claim(CLAIM_USER_ACCOUNT,  token.getUsername())
                .claim(CLAIM_DEPT_ID,       String.valueOf(token.getDeptId()))
                .claim(CLAIM_DEPT_CODE,     token.getDeptCode())
                .claim(CLAIM_DEPT_NAME,     token.getDeptName())
                .claim(CLAIM_CLUSTER_ID,    token.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, token.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME,  token.getClusterName())
                .claim(CLAIM_USER_ROLE,     token.getRoles())
                .claim(CLAIM_USER_PERM,     token.getPermissions())
                .claim(CLAIM_TYPE,          token.getType())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessConfiguration.tokenSalt())
                .setExpiration(new Date(System.currentTimeMillis() + accessConfiguration.tokenAccessExpire() * 1000L))
                .compact();
        token.setAccessToken(accessToken);

        String refreshToken = Jwts.builder()
                .claim(CLAIM_ID,      token.getId())
                .claim(CLAIM_TYPE,       token.getType())
                .claim(CLAIM_USER_ACCOUNT,  token.getUsername())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessConfiguration.tokenSalt())
                .compact();
        token.setRefreshToken(refreshToken);

        assert redis != null;
        String key = AccessToken.KEY + token.getType() + ":" + token.getUsername();
        redis.putExpireValue(key, token, accessConfiguration.tokenRefreshExpire(), TimeUnit.SECONDS);

        Access access = Access.get();
        if(access != null){
            access.setAccessToken(token);
        }
    }

    /**
     * 刷新Token
     */
    public void refreshToken(HttpServletResponse response, String refreshToken) throws Exception {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(accessConfiguration.tokenSalt()).parseClaimsJws(refreshToken).getBody();
        } catch(Exception e) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.invalid");
            return;
        }
        // 获取服务保存的Token
        assert redis != null;
        String userAccount = (String)claims.get(CLAIM_USER_ACCOUNT);
        AccessToken accessToken = redis.getValue(getKey(claims) + userAccount);
        if(accessToken == null) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.notexist");
            return;
        }
        // 比对id，判断Token是否已经被刷新过
        String tokenId = (String)claims.get(CLAIM_ID);
        if(accessConfiguration.tokenConflict() && !tokenId.equals(accessToken.getId())) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.conflict");
            return;
        }
        // 更新Token信息
        accessToken.setId(IdUtil.fastSimpleUUID());
        accessToken.setAccessTime(Access.accessTime());
        accessToken.setAccessIp(Access.accessIp());
        // 刷新Token并返回
        assignToken(accessToken);
        response.getWriter().write(JSON.toJSONString(Response.success(accessToken)));
    }

    /**
     * 解析Token
     */
    public AccessToken parseToken(HttpServletRequest request) {
        String jwt = getJwt(request);
        if(jwt == null) {
            return new AccessToken(UNAUTHORIZED, Messages.msg("frame.auth.no"));
        }
        return parseJwt(jwt);
    }

    public String getJwt(HttpServletRequest request) {
        String jwt = request.getHeader(accessConfiguration.tokenHeader());
        if(StringUtils.isEmpty(jwt)) {
            return null;
        }
        if(jwt.startsWith("Bearer ")) {
            jwt = jwt.replace("Bearer ", "");
        }
        return jwt;
    }

    @SuppressWarnings("unchecked")
    public AccessToken parseJwt(String jwt) {
        Claims claims;
        try {
            claims =  Jwts.parser().setSigningKey(accessConfiguration.tokenSalt()).parseClaimsJws(jwt).getBody();
        }catch(ExpiredJwtException e) {
            return new AccessToken(TOKEN_INVALID_OR_EXPIRED, Messages.msg("frame.auth.expired"));
        }catch(Exception e) {
            return new AccessToken(UNAUTHORIZED, Messages.msg("frame.auth.invalid"));
        }

        // IP变化，要求重新刷一下accessToken
        String userIp = (String)claims.get(CLAIM_USER_IP);
        if(accessConfiguration.tokenConflict() && !Objects.equals(Access.accessIp(), userIp)) {
            return new AccessToken(TOKEN_INVALID_OR_EXPIRED, Messages.msg("frame.auth.ipchanged"));
        }

        AccessToken accessToken = new AccessToken();
        // token
        accessToken.setAccessToken(jwt);
        accessToken.setId((String)claims.get(CLAIM_ID));
        accessToken.setType((String)claims.get(CLAIM_TYPE));

        // user
        String userId = (String)claims.get(CLAIM_USER_ID);
        if(StringUtils.isNotBlank(userId) && !"null".equals(userId)) {
            accessToken.setUserId(Long.valueOf(userId));
        }
        accessToken.setUserCode((String)claims.get(CLAIM_USER_CODE));
        accessToken.setUsername((String)claims.get(CLAIM_USER_ACCOUNT));
        accessToken.setUserNick((String)claims.get(CLAIM_USER_NAME));

        // dept
        String deptId = (String)claims.get(CLAIM_DEPT_ID);
        if(StringUtils.isNotBlank(deptId) && !"null".equals(deptId)) {
            accessToken.setDeptId(Long.valueOf(deptId));
        }
        accessToken.setDeptCode((String)claims.get(CLAIM_DEPT_CODE));
        accessToken.setDeptName((String)claims.get(CLAIM_DEPT_NAME));

        // cluster
        accessToken.setClusterId((Integer)claims.get(CLAIM_CLUSTER_ID));
        accessToken.setClusterLevel((Integer)claims.get(CLAIM_CLUSTER_LEVEL));
        accessToken.setClusterName((String)claims.get(CLAIM_CLUSTER_NAME));

        // roles
        accessToken.setRoles((List<String>)claims.get(CLAIM_USER_ROLE));
        // permits
        accessToken.setPermissions((List<String>)claims.get(CLAIM_USER_PERM));

        Access access = Access.get();
        if(access != null){
            access.setAccessToken(accessToken);
        }
        return accessToken;
    }

    /**
     * 删除Token
     */
    public void deleteToken(HttpServletResponse response, String accessToken) throws IOException {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(accessConfiguration.tokenSalt()).parseClaimsJws(accessToken).getBody();
        }catch(ExpiredJwtException e) {
            claims = e.getClaims();
        }catch(Exception e) {
            writeResponse(response, UNAUTHORIZED, "frame.auth.invalid");
            return;
        }
        assert redis != null;
        String userAccount = (String)claims.get(CLAIM_USER_ACCOUNT);
        redis.delete((getKey(claims) + userAccount));
    }

    private String getKey(Claims claims){
        String tokenType = (String)claims.get(CLAIM_TYPE);
        return AccessToken.KEY + tokenType + ":";
    }

    public boolean validAccessToken(String accessToken) {
        if(StringUtils.isBlank(accessToken)) {
            return false;
        }
        if(accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.replace("Bearer ", "");
        }
        try {
            Jwts.parser().setSigningKey(accessConfiguration.tokenSalt()).parseClaimsJws(accessToken).getBody();
        }catch(Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 创建AccessToken
     */
    public String createAccessToken(AccessToken token, int accessExpire) {
        return Jwts.builder()
                .claim(CLAIM_USER_ID,       String.valueOf(token.getUserId())) // Long取出来是Integer，干脆用String处理
                .claim(CLAIM_USER_CODE,     token.getUserCode())
                .claim(CLAIM_USER_NAME,     token.getUserNick())
                .claim(CLAIM_USER_ACCOUNT,  token.getUsername())
                .claim(CLAIM_DEPT_ID,       String.valueOf(token.getDeptId()))
                .claim(CLAIM_DEPT_CODE,     token.getDeptCode())
                .claim(CLAIM_DEPT_NAME,     token.getDeptName())
                .claim(CLAIM_CLUSTER_ID,    token.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, token.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME,  token.getClusterName())
                .claim(CLAIM_USER_ROLE,     token.getRoles())
                .claim(CLAIM_USER_PERM,     token.getPermissions())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, accessConfiguration.tokenSalt())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpire * 1000L))
                .compact();
    }

    private void writeResponse(HttpServletResponse response, ResponseCode responseCode, String messageKey) throws IOException {
        int httpStatus = responseCode.getStatus();
        if(accessConfiguration.isAlwaysSuccess()){
            httpStatus = SUCCESS.getStatus();
        }
        response.setStatus(httpStatus);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try(PrintWriter writer = response.getWriter()){
            writer.write(JSON.toJSONString(Response.msg(responseCode, Messages.msg(messageKey))));
        }
    }
}
