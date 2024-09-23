package com.cowave.commons.framework.filter.security;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.support.redis.RedisHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.feign.codec.Response;
import org.springframework.feign.codec.ResponseCode;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Service;

import com.cowave.commons.framework.helper.MessageHelper;

import cn.hutool.core.util.IdUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

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

    private final MessageHelper message;

    private final TokenConfiguration configuration;

    @Nullable
    private final RedisHelper redis;

    public void setToken(AccessToken accessToken) {
        accessToken.setToken(newToken(accessToken));
        String key = AccessToken.KEY + accessToken.getType() + ":" + accessToken.getUsername();
        redis.putExpireValue(key, accessToken, configuration.getServerExpire(), TimeUnit.SECONDS);
        Access access = Access.get();
        access.setAccessToken(accessToken);
    }

    public AccessToken parseToken(HttpServletRequest request) {
        String jwt = getJwt(request);
        if(jwt == null) {
            return new AccessToken(401, message.msg("frame.auth.no"));
        }
        return parseJwt(jwt);
    }

    public String getJwt(HttpServletRequest request) {
        String jwt = request.getHeader(configuration.getHeader());
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
        Access access = Access.get();
        Claims claims;
        try {
            claims =  Jwts.parser().setSigningKey(configuration.getSalt()).parseClaimsJws(jwt).getBody();
        }catch(ExpiredJwtException e) {
            AccessToken expiredToken = new AccessToken(498, message.msg("frame.auth.expired"), jwt);
            access.setAccessToken(expiredToken);
            return expiredToken;
        }catch(Exception e) {
            return new AccessToken(401, message.msg("frame.auth.invalid"));
        }

        String userIp = (String)claims.get(CLAIM_USER_IP);
        if(configuration.isConflict() && !Objects.equals(Access.ip(), userIp)) {
            AccessToken changedToken = new AccessToken(498, message.msg("frame.auth.ipchanged"), jwt);
            access.setAccessToken(changedToken);
            return changedToken;
        }

        AccessToken accessToken = new AccessToken();
        // token
        accessToken.setToken(jwt);
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

        access.setAccessToken(accessToken);
        return accessToken;
    }

    public void refreshToken(HttpServletResponse response, String jwt) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(ResponseCode.OK.getCode());
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(configuration.getSalt()).parseClaimsJws(jwt).getBody();
        }catch(ExpiredJwtException e) {
            claims = e.getClaims();
        }catch(Exception e) {
            response.getWriter().write(JSON.toJSONString(
                    Response.error(ResponseCode.UNAUTHORIZED, message.msg("frame.auth.invalid"))));
            return;
        }

        String userAccount = (String)claims.get(CLAIM_USER_ACCOUNT);
        AccessToken accessToken = redis.getValue(getKey(claims) + userAccount);
        if(accessToken == null) {
            response.getWriter().write(JSON.toJSONString(
                    Response.error(ResponseCode.UNAUTHORIZED, message.msg("frame.auth.notexist"))));
            return;
        }

        String tokenId = (String)claims.get(CLAIM_ID);
        if(configuration.isConflict() && !tokenId.equals(accessToken.getId())) {
            response.getWriter().write(JSON.toJSONString(
                    Response.error(ResponseCode.UNAUTHORIZED, message.msg("frame.auth.conflict"))));
            return;
        }

        accessToken.setId(IdUtil.fastSimpleUUID());
        accessToken.setAccessTime(Access.time());
        accessToken.setAccessIp(Access.ip());
        accessToken.setToken(newToken(accessToken));
        redis.putExpireValue(getKey(claims) + userAccount, accessToken, configuration.getServerExpire(), TimeUnit.SECONDS);

        Access access = Access.get();
        access.setAccessToken(accessToken);
        response.getWriter().write(JSON.toJSONString(Response.success(accessToken.getToken())));
    }

    public void deleteToken(HttpServletResponse response, String jwt) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(ResponseCode.OK.getCode());
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(configuration.getSalt()).parseClaimsJws(jwt).getBody();
        }catch(ExpiredJwtException e) {
            claims = e.getClaims();
        }catch(Exception e) {
            response.getWriter().write(JSON.toJSONString(
                    Response.error(ResponseCode.UNAUTHORIZED, message.msg("frame.auth.invalid"))));
            return;
        }
        String userAccount = (String)claims.get(CLAIM_USER_ACCOUNT);
        redis.delete((getKey(claims) + userAccount));
    }

    private String getKey(Claims claims){
        String tokenType = (String)claims.get(CLAIM_TYPE);
        return AccessToken.KEY + tokenType + ":";
    }

    public String newToken(AccessToken accessToken) {
        int expireSec = accessToken.getExpire(configuration);
        return Jwts.builder()
                .claim(CLAIM_ID,           accessToken.getId())
                .claim(CLAIM_TYPE,         accessToken.getType())
                .claim(CLAIM_USER_IP,      accessToken.getAccessIp())
                .claim(CLAIM_USER_ID, String.valueOf(accessToken.getUserId())) // Long取出来是Integer，干脆用String处理
                .claim(CLAIM_USER_CODE,    accessToken.getUserCode())
                .claim(CLAIM_USER_NAME,    accessToken.getUserNick())
                .claim(CLAIM_USER_ACCOUNT, accessToken.getUsername())
                .claim(CLAIM_DEPT_ID, String.valueOf(accessToken.getDeptId()))
                .claim(CLAIM_DEPT_CODE,    accessToken.getDeptCode())
                .claim(CLAIM_DEPT_NAME,    accessToken.getDeptName())
                .claim(CLAIM_CLUSTER_ID,   accessToken.getClusterId())
                .claim(CLAIM_CLUSTER_LEVEL, accessToken.getClusterLevel())
                .claim(CLAIM_CLUSTER_NAME,  accessToken.getClusterName())
                .claim(CLAIM_USER_ROLE,    accessToken.getRoles())
                .claim(CLAIM_USER_PERM,    accessToken.getPermissions())
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS512, configuration.getSalt())
                .setExpiration(new Date(System.currentTimeMillis() + expireSec * 1000L))
                .compact();
    }

    public boolean validAuthorization(String authorization) {
        if(StringUtils.isBlank(authorization)) {
            return false;
        }
        if(authorization.startsWith("Bearer ")) {
            authorization = authorization.replace("Bearer ", "");
        }
        try {
            Jwts.parser().setSigningKey(configuration.getSalt()).parseClaimsJws(authorization).getBody();
        }catch(ExpiredJwtException e) {
            return true;
        }catch(Exception e) {
            return false;
        }
        return true;
    }
}
