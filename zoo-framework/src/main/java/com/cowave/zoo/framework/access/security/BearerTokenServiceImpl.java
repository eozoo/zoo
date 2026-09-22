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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ZSetOperations;
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
    // {applicationName}:auth:access:{userAccount}:{sessionId}:{tenantCode}:{accessId}
    public static final String AUTH_ACCESS_KEY = "%s:auth:access:%s:%s:%s:%s";
    // {applicationName}:auth:refresh:{userAccount}:{sessionId}
    public static final String AUTH_REFRESH_KEY = "%s:auth:refresh:%s:%s";
    // {applicationName}:auth:oauth:{userAccount}:{sessionId}:{tenantCode}:{appId}
    public static final String AUTH_OAUTH_KEY = "%s:auth:oauth:%s:%s:%s:%s";

    // 全局在线会话索引ZSET：索引系统内所有持有有效Refresh Token的登录会话，支持全局在线查询
    // {applicationName}:auth:index member={userAccount}:{sessionId} score=loginTime
    public static final String ONLINE_INDEX_KEY = "%s:auth:index";

    // 租户在线会话索引ZSET：索引当前授权上下文位于指定租户的登录会话，支持租户在线查询
    // {applicationName}:auth:tenant:{tenantCode}:index member={userAccount}:{sessionId} score=loginTime
    public static final String TENANT_ONLINE_INDEX_KEY = "%s:auth:tenant:%s:index";

    // 会话授权令牌索引SET：索引一个登录会话发放的全部Access/OAuth Token，用于令牌查询和批量撤销
    // {applicationName}:auth:index:{userAccount}:{sessionId}
    // member=access:{tenantCode}:{accessId} 或 member=oauth:{tenantCode}:{appId}
    public static final String GRANT_INDEX_KEY = "%s:auth:index:%s:%s";

    // 用户登录会话索引SET：索引一个用户的全部登录设备/会话，用于单端互斥和用户全部下线
    // {applicationName}:auth:session:{userAccount} member={sessionId}
    public static final String SESSION_INDEX_KEY = "%s:auth:session:%s";

    // 在线索引的成员分隔符，解析时取最后一个分隔符
    private static final char MEMBER_SEPARATOR = ':';
    // 在线索引数据类型
    private static final String GRANT_ACCESS = "access:";
    private static final String GRANT_OAUTH = "oauth:";

    private final RedisHelper redisHelper;
    private final ObjectMapper objectMapper;
    private final AccessIdGenerator accessIdGenerator;
    private final BearerTokenDelegate bearerTokenDelegate;

    @Override
    public void assignAccessToken(AccessUserDetails userDetails) {
        ensureSessionId(userDetails);
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
            String userAccount = userDetails.getUsername();
            String sessionId = userDetails.getSessionId();
            // 仅使用accessToken且不允许同时登录，那么删掉其它令牌
            if(userDetails.isAccessUnique() && !useRefreshToken){
                revokeGrantToken(userAccount, sessionId, GRANT_ACCESS);
            }
            // 记录本次发放的令牌
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(userDetails);
            redisHelper.putExpire(getAccessTokenKey(userDetails), accessTokenInfo, accessExpire, TimeUnit.SECONDS);
            // 添加在线用户令牌索引数据
            indexGrant(userAccount, sessionId,
                    accessGrant(userDetails.getTenantCode(), userDetails.getAccessId()));
        }
    }

    @Override
    public void assignAccessRefreshToken(AccessUserDetails userDetails) {
        ensureSessionId(userDetails);
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
            if (userDetails.isAccessUnique()) {
                revokeOtherSessions(userDetails.getUsername(), userDetails.getSessionId());
            }
            String refreshKey = getRefreshTokenKey(userDetails);
            RefreshTokenInfo previousTokenInfo = redisHelper.getValue(refreshKey);
            if (previousTokenInfo != null
                    && !Objects.equals(previousTokenInfo.getTenantCode(), userDetails.getTenantCode())) {
                unIndexTenantOnline(previousTokenInfo.getTenantCode(),
                        userDetails.getUsername(), userDetails.getSessionId());
            }
            RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(userDetails);
            redisHelper.putExpire(refreshKey, refreshTokenInfo, refreshExpire, TimeUnit.SECONDS);
            // 添加在线用户索引数据
            indexOnline(userDetails.getUsername(), userDetails.getSessionId(),
                    userDetails.getTenantCode(), refreshTokenInfo.getLoginTime());
            indexSession(userDetails.getUsername(), userDetails.getSessionId());
        }
    }

    @Override
    public void assignOauthToken(AccessUserDetails userDetails) {
        ensureSessionId(userDetails);
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
            String oauthKey = getOauthTokenKey(userDetails.getUsername(), userDetails.getSessionId(),
                    userDetails.getTenantCode(), userDetails.getOauthId());
            redisHelper.putExpire(oauthKey, refreshTokenInfo, refreshExpire, TimeUnit.SECONDS);
            // 添加在线用户令牌索引数据
            indexGrant(userDetails.getUsername(), userDetails.getSessionId(),
                    oauthGrant(userDetails.getTenantCode(), userDetails.getOauthId()));
        }
    }

    @Override
    public String refreshAccessToken() throws Exception {
        AccessUserDetails userDetails = parseAccessToken(null);
        if (userDetails.isAccessValid() && redisHelper != null) {
            redisHelper.delete(getAccessTokenKey(userDetails));
            unIndexGrant(userDetails.getUsername(), userDetails.getSessionId(),
                    accessGrant(userDetails.getTenantCode(), userDetails.getAccessId()));
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
        String refreshTokenKey = getRefreshTokenKey(details.getUsername(), details.getSessionId());
        RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(refreshTokenKey);
        if (refreshTokenInfo == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 比对id，判断Token是否已经被刷新过
        if (!Objects.equals(details.getRefreshId(), refreshTokenInfo.getRefreshId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }

        //当前accessToken删除
        String accessId = refreshTokenInfo.getAccessId();
        if (details.isAccessValid()) {
            String accessTokenKey = getAccessTokenKey(details.getUsername(), details.getSessionId(),
                    refreshTokenInfo.getTenantCode(), accessId);
            redisHelper.delete(accessTokenKey);
            unIndexGrant(details.getUsername(), details.getSessionId(),
                    accessGrant(refreshTokenInfo.getTenantCode(), accessId));
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
                details.getUsername(), details.getSessionId(), details.getTenantCode(), details.getOauthId());
        RefreshTokenInfo oauthTokenInfo = redisHelper.getValue(oauthTokenKey);
        if (oauthTokenInfo == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 比对id，判断Token是否已经被刷新过
        if (!Objects.equals(details.getRefreshId(), oauthTokenInfo.getRefreshId())) {
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
            return doParseAccessToken(accessToken, response);
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

    private AccessUserDetails doParseAccessToken(String accessToken, HttpServletResponse response) throws IOException {
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
        AccessUserDetails userDetails = bearerTokenDelegate.parseAccessClaims(claims);
        boolean validated = validateUserDetails(userDetails, response, false);
        if(validated) {
            // 保存到上下文中
            Access access = Access.get();
            if (access == null) {
                access = Access.newAccess(accessIdGenerator);
            }
            access.setUserDetails(userDetails);
            Access.set(access);
            return userDetails;
        }
        return null;
    }

    protected boolean validateUserDetails(AccessUserDetails userDetails, HttpServletResponse response, boolean useRefreshToken) throws IOException {
        if (useRefreshToken) {
            // IP变化，要求重新刷一下accessToken
            if (userDetails.isAccessUnique() && !Objects.equals(Access.accessIp(), userDetails.getAccessIp())) {
                writeResponse(response, INVALID_TOKEN, "frame.auth.access.changed.ip");
                return false;
            }
        }
        // 服务端校验AccessToken
        if (userDetails.isAccessValid()) {
            if (useRefreshToken) {
                RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(getRefreshTokenKey(userDetails));
                // 确认refreshTokenInfo存在
                if (refreshTokenInfo == null) {
                    writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                    return false;
                }

                // 确认Refresh Token仍是当前会话的最新令牌
                if (!Objects.equals(userDetails.getRefreshId(), refreshTokenInfo.getRefreshId())) {
                    writeResponse(response, UNAUTHORIZED, "frame.auth.refresh.changed");
                    return false;
                }

                // 允许同时登录，检查是否手动标记注销
                AccessTokenInfo accessTokenInfo = redisHelper.getValue(getAccessTokenKey(userDetails));
                if(accessTokenInfo == null || accessTokenInfo.getRevoked() == 1){
                    writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                    return false;
                }
            }else{
                AccessTokenInfo accessTokenInfo = redisHelper.getValue(getAccessTokenKey(userDetails));
                // 确认accessTokenInfo存在
                if (accessTokenInfo == null) {
                    writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public AccessUserDetails parseAccessRefreshToken(HttpServletResponse response) throws IOException {
        String accessToken = getAccessToken();
        if (accessToken != null) {
            return doParseAccessRefreshToken(accessToken, response);
        }
        writeResponse(response, UNAUTHORIZED, "frame.auth.access.empty");
        return null;
    }

    private AccessUserDetails doParseAccessRefreshToken(String accessToken, HttpServletResponse response) throws IOException {
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
        AccessUserDetails userDetails = bearerTokenDelegate.parseAccessClaims(claims);
        boolean validated = validateUserDetails(userDetails, response, true);
        if(validated) {
            // 保存到上下文中
            Access access = Access.get();
            if (access == null) {
                access = Access.newAccess(accessIdGenerator);
            }
            access.setUserDetails(userDetails);
            Access.set(access);
            return userDetails;
        }
        return null;
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
            unIndexGrant(userDetails.getUsername(), userDetails.getSessionId(),
                    accessGrant(userDetails.getTenantCode(), userDetails.getAccessId()));
        }

        if (ACCESS_REFRESH == bearerTokenDelegate.authMode()) {
            revokeRefreshToken(userDetails.getUsername(), userDetails.getSessionId());
        }
    }

    @Override
    public AccessTokenInfo revokeAccessToken(String userAccount, String sessionId, String tenantCode, String accessId) {
        String accesskey = getAccessTokenKey(userAccount, sessionId, tenantCode, accessId);
        AccessTokenInfo accessTokenInfo = redisHelper.getValue(accesskey);
        redisHelper.delete(accesskey);
        unIndexGrant(userAccount, sessionId, accessGrant(tenantCode, accessId));
        return accessTokenInfo;
    }

    @Override
    public RefreshTokenInfo revokeRefreshToken(String userAccount, String sessionId) {
        String refreshKey = getRefreshTokenKey(userAccount, sessionId);
        RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(refreshKey);
        redisHelper.delete(refreshKey);
        // 按授权索引删除令牌
        String grantKey = getGrantIndexKey(userAccount, sessionId);
        List<String> tokenKeys = grantTokenKeys(userAccount, sessionId, redisHelper.getSet(grantKey), null);
        if (!tokenKeys.isEmpty()) {
            redisHelper.delete(tokenKeys);
        }
        redisHelper.delete(grantKey);
        redisHelper.removeFromZset(getOnlineIndexKey(), onlineMember(userAccount, sessionId));
        if (refreshTokenInfo != null) {
            unIndexTenantOnline(refreshTokenInfo.getTenantCode(), userAccount, sessionId);
        }
        redisHelper.removeFromSet(getSessionIndexKey(userAccount), sessionId);
        return refreshTokenInfo;
    }

    @Override
    public void revokeUserTokens(String userAccount) {
        String sessionIndexKey = getSessionIndexKey(userAccount);
        Set<String> sessionIds = redisHelper.getSet(sessionIndexKey);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                revokeRefreshToken(userAccount, sessionId);
            }
        }
        redisHelper.delete(sessionIndexKey);
    }

    @Override
    public RefreshTokenInfo revokeOauthToken(String userAccount, String sessionId, String tenantCode, String appId) {
        String oauthkey = getOauthTokenKey(userAccount, sessionId, tenantCode, appId);
        RefreshTokenInfo oauthToken = redisHelper.getValue(oauthkey);
        redisHelper.delete(oauthkey);
        unIndexGrant(userAccount, sessionId, oauthGrant(tenantCode, appId));
        return oauthToken;
    }

    @Override
    public List<OnlineIndex> listOnlineIndex(Date beginTime, Date endTime) {
        return listOnlineIndex(getOnlineIndexKey(), null, beginTime, endTime);
    }

    @Override
    public List<OnlineIndex> listTenantOnlineIndex(String tenantCode, Date beginTime, Date endTime) {
        return listOnlineIndex(getTenantOnlineIndexKey(tenantCode), tenantCode, beginTime, endTime);
    }

    private List<OnlineIndex> listOnlineIndex(String onlineIndexKey, String tenantCode, Date beginTime, Date endTime) {
        Set<ZSetOperations.TypedTuple<String>> tuples;
        if (beginTime == null && endTime == null) {
            tuples = redisHelper.reverseRangeOfZsetWithScores(onlineIndexKey, 0, -1);
        } else {
            double min = beginTime == null ? Double.NEGATIVE_INFINITY : beginTime.getTime();
            double max = endTime == null ? Double.POSITIVE_INFINITY : endTime.getTime();
            tuples = redisHelper.reverseRangeOfZsetByScoreWithScores(onlineIndexKey, min, max);
        }

        List<OnlineIndex> list = new ArrayList<>();
        if (tuples == null) {
            return list;
        }

        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String value = tuple.getValue();
            if (value == null) {
                continue;
            }

            int index = value.lastIndexOf(MEMBER_SEPARATOR);
            if (index < 0) {
                continue;
            }

            Double score = tuple.getScore();
            list.add(new OnlineIndex(value.substring(0, index), value.substring(index + 1), tenantCode,
                    score == null ? null : new Date(score.longValue())));
        }
        return list;
    }

    @Override
    public List<OnlineToken> listOnlineToken(List<OnlineIndex> indexList) {
        List<OnlineToken> onlineTokens = new ArrayList<>();
        if (indexList == null || indexList.isEmpty()) {
            return onlineTokens;
        }

        // MGET
        List<String> refreshKeys = new ArrayList<>(indexList.size());
        for (OnlineIndex index : indexList) {
            refreshKeys.add(getRefreshTokenKey(index.getUserAccount(), index.getSessionId()));
        }

        List<RefreshTokenInfo> refreshTokenList = redisHelper.getMultiValue(refreshKeys);
        if (CollectionUtils.isEmpty(refreshTokenList)) {
            return onlineTokens;
        }

        List<OnlineIndex> livedList = new ArrayList<>();
        List<Object> expiredList = new ArrayList<>();
        for (int i = 0; i < indexList.size(); i++) {
            OnlineIndex index = indexList.get(i);
            RefreshTokenInfo refreshToken = refreshTokenList.get(i);
            // 令牌过期，索引还在
            if (refreshToken == null) {
                expiredList.add(onlineMember(index.getUserAccount(), index.getSessionId()));
                unIndexTenantOnline(index.getTenantCode(), index.getUserAccount(), index.getSessionId());
                redisHelper.removeFromSet(getSessionIndexKey(index.getUserAccount()), index.getSessionId());
                continue;
            }
            if (StringUtils.isNotBlank(index.getTenantCode())
                    && !Objects.equals(index.getTenantCode(), refreshToken.getTenantCode())) {
                unIndexTenantOnline(index.getTenantCode(), index.getUserAccount(), index.getSessionId());
                continue;
            }
            index.setTenantCode(refreshToken.getTenantCode());
            livedList.add(index);
            onlineTokens.add(new OnlineToken(refreshToken));
        }

        // 清除过期索引
        if (!expiredList.isEmpty()) {
            redisHelper.removeFromZset(getOnlineIndexKey(), expiredList.toArray());
        }

        // 补充令牌信息
        if (!livedList.isEmpty()) {
            fillGrantToken(livedList, onlineTokens);
        }
        return onlineTokens;
    }

    private void fillGrantToken(List<OnlineIndex> indexList, List<OnlineToken> onlineTokens) {
        List<OnlineRecord> records = new ArrayList<>();
        for (int i = 0; i < indexList.size(); i++) {
            OnlineIndex index = indexList.get(i);
            // 令牌索引数据key
            String grantIndexKey = getGrantIndexKey(index.getUserAccount(), index.getSessionId());
            Set<String> grantIndexSet = redisHelper.getSet(grantIndexKey);
            if (grantIndexSet == null) {
                continue;
            }

            for (String grantIndex : grantIndexSet) {
                // 令牌key grant格式 access:accessId 或 oauth:oauthId
                String grantTokenKey = getGrantTokenKey(
                        index.getUserAccount(), index.getSessionId(), grantIndex);
                if (grantTokenKey != null) {
                    records.add(new OnlineRecord(i, grantIndexKey, grantIndex, grantTokenKey));
                }
            }
        }
        if (records.isEmpty()) {
            return;
        }

        List<String> tokenKeys = new ArrayList<>(records.size());
        for (OnlineRecord record : records) {
            tokenKeys.add(record.grantTokenKey());
        }
        List<Object> tokens = redisHelper.getMultiValue(tokenKeys);
        if (CollectionUtils.isEmpty(tokens)) {
            return;
        }

        Map<String, List<Object>> expiredGrants = new HashMap<>();
        for (int i = 0; i < records.size(); i++) {
            OnlineRecord record = records.get(i);
            Object token = tokens.get(i);
            if (token == null) {
                // 令牌过期，索引还在
                expiredGrants.computeIfAbsent(record.grantKey(), key -> new ArrayList<>()).add(record.grant());
                continue;
            }

            OnlineToken onlineToken = onlineTokens.get(record.ownerIndex());
            if (record.grant().startsWith(GRANT_ACCESS)) {
                onlineToken.getAccessTokens().add((AccessTokenInfo) token);
            } else {
                onlineToken.getOauthTokens().add((RefreshTokenInfo) token);
            }
        }
        // 清除过期索引
        expiredGrants.forEach((grantKey, grants) -> redisHelper.removeFromSet(grantKey, grants.toArray()));
    }

    // 添加在线会话索引，在线状态以Refresh Token是否有效为准
    private void indexOnline(String userAccount, String sessionId, String tenantCode, Date loginTime) {
        String onlineMember = onlineMember(userAccount, sessionId);
        double loginTimestamp = loginTime == null ? 0D : loginTime.getTime();
        int refreshExpire = bearerTokenDelegate.getRefreshExpireSeconds();

        String onlineKey = getOnlineIndexKey();
        redisHelper.putZset(onlineKey, onlineMember, loginTimestamp);
        redisHelper.expire(onlineKey, refreshExpire, TimeUnit.SECONDS);

        if (StringUtils.isNotBlank(tenantCode)) {
            String tenantOnlineKey = getTenantOnlineIndexKey(tenantCode);
            redisHelper.putZset(tenantOnlineKey, onlineMember, loginTimestamp);
            redisHelper.expire(tenantOnlineKey, refreshExpire, TimeUnit.SECONDS);
        }
    }

    private void unIndexTenantOnline(String tenantCode, String userAccount, String sessionId) {
        if (StringUtils.isNotBlank(tenantCode)) {
            redisHelper.removeFromZset(getTenantOnlineIndexKey(tenantCode),
                    onlineMember(userAccount, sessionId));
        }
    }

    // 添加用户会话索引
    private void indexSession(String userAccount, String sessionId) {
        String sessionKey = getSessionIndexKey(userAccount);
        redisHelper.offerSet(sessionKey, sessionId);
        redisHelper.expire(sessionKey, bearerTokenDelegate.getRefreshExpireSeconds(), TimeUnit.SECONDS);
    }

    // 添加会话令牌索引数据
    private void indexGrant(String userAccount, String sessionId, String grant) {
        String grantKey = getGrantIndexKey(userAccount, sessionId);
        redisHelper.offerSet(grantKey, grant);
        redisHelper.expire(grantKey, bearerTokenDelegate.getRefreshExpireSeconds(), TimeUnit.SECONDS);
    }

    // 删除令牌索引
    private void unIndexGrant(String userAccount, String sessionId, String grant) {
        redisHelper.removeFromSet(getGrantIndexKey(userAccount, sessionId), grant);
    }

    // 撤销会话下指定类型的令牌
    private void revokeGrantToken(String userAccount, String sessionId, String grantPrefix) {
        String grantIndexKey = getGrantIndexKey(userAccount, sessionId);
        Set<String> grantIndexSet = redisHelper.getSet(grantIndexKey);
        if (CollectionUtils.isEmpty(grantIndexSet)) {
            return;
        }

        List<String> tokenKeys = grantTokenKeys(userAccount, sessionId, grantIndexSet, grantPrefix);
        if (tokenKeys.isEmpty()) {
            return;
        }

        // 删除令牌
        redisHelper.delete(tokenKeys);
        // 删除索引
        List<Object> revokedIndex = new ArrayList<>();
        for (String grantIndex : grantIndexSet) {
            if (grantIndex.startsWith(grantPrefix)) {
                revokedIndex.add(grantIndex);
            }
        }
        redisHelper.removeFromSet(grantIndexKey, revokedIndex.toArray());
    }

    // accessUnique仅限制并行会话，不再参与Refresh Token轮换校验
    private void revokeOtherSessions(String userAccount, String currentSessionId) {
        Set<String> sessionIds = redisHelper.getSet(getSessionIndexKey(userAccount));
        if (CollectionUtils.isEmpty(sessionIds)) {
            return;
        }

        for (String sessionId : new HashSet<>(sessionIds)) {
            if (!Objects.equals(sessionId, currentSessionId)) {
                revokeRefreshToken(userAccount, sessionId);
            }
        }
    }

    // 令牌索引转换成令牌Key
    private List<String> grantTokenKeys(String userAccount, String sessionId,
                                        Set<String> grantIndexSet, String grantPrefix) {
        List<String> tokenKeys = new ArrayList<>();
        if (grantIndexSet == null) {
            return tokenKeys;
        }

        for (String grantIndex : grantIndexSet) {
            if (grantPrefix != null && !grantIndex.startsWith(grantPrefix)) {
                continue;
            }

            String tokenKey = getGrantTokenKey(userAccount, sessionId, grantIndex);
            if (tokenKey != null) {
                tokenKeys.add(tokenKey);
            }
        }
        return tokenKeys;
    }

    private static String accessGrant(String tenantCode, String accessId) {
        return GRANT_ACCESS + tenantCode + MEMBER_SEPARATOR + accessId;
    }

    private static String oauthGrant(String tenantCode, String appId) {
        return GRANT_OAUTH + tenantCode + MEMBER_SEPARATOR + appId;
    }

    private static String onlineMember(String userAccount, String sessionId) {
        return userAccount + MEMBER_SEPARATOR + sessionId;
    }

    private String getOnlineIndexKey() {
        return ONLINE_INDEX_KEY.formatted(bearerTokenDelegate.getRefreshIssuer());
    }

    private String getTenantOnlineIndexKey(String tenantCode) {
        return TENANT_ONLINE_INDEX_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(), tenantCode);
    }

    private String getSessionIndexKey(String userAccount) {
        return SESSION_INDEX_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(), userAccount);
    }

    private String getGrantIndexKey(String userAccount, String sessionId) {
        return GRANT_INDEX_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(), userAccount, sessionId);
    }

    private String getGrantTokenKey(String userAccount, String sessionId, String grantIndex) {
        String grantPrefix;
        if (grantIndex.startsWith(GRANT_ACCESS)) {
            grantPrefix = GRANT_ACCESS;
        } else if (grantIndex.startsWith(GRANT_OAUTH)) {
            grantPrefix = GRANT_OAUTH;
        } else {
            return null;
        }

        String grant = grantIndex.substring(grantPrefix.length());
        int separator = grant.indexOf(MEMBER_SEPARATOR);
        if (separator <= 0 || separator == grant.length() - 1) {
            return null;
        }

        String tenantCode = grant.substring(0, separator);
        String tokenId = grant.substring(separator + 1);
        if (GRANT_ACCESS.equals(grantPrefix)) {
            return getAccessTokenKey(userAccount, sessionId, tenantCode, tokenId);
        }
        return getOauthTokenKey(userAccount, sessionId, tenantCode, tokenId);
    }

    private String getAccessTokenKey(AccessUserDetails userDetails) {
        return getAccessTokenKey(userDetails.getUsername(), userDetails.getSessionId(),
                userDetails.getTenantCode(), userDetails.getAccessId());
    }

    private String getAccessTokenKey(String userAccount, String sessionId,
                                     String tenantCode, String accessId) {
        return AUTH_ACCESS_KEY.formatted(bearerTokenDelegate.getAccessIssuer(),
                userAccount, sessionId, tenantCode, accessId);
    }

    private String getRefreshTokenKey(AccessUserDetails userDetails) {
        return getRefreshTokenKey(userDetails.getUsername(), userDetails.getSessionId());
    }

    private String getRefreshTokenKey(String userAccount, String sessionId) {
        return AUTH_REFRESH_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(), userAccount, sessionId);
    }

    private String getOauthTokenKey(String userAccount, String sessionId,
                                    String tenantCode, String appId) {
        return AUTH_OAUTH_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(),
                userAccount, sessionId, tenantCode, appId);
    }

    private static void ensureSessionId(AccessUserDetails userDetails) {
        if (StringUtils.isBlank(userDetails.getSessionId())) {
            userDetails.setSessionId(IdUtil.fastSimpleUUID());
        }
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

    @Override
    public AccessUserDetails validateSocketAccessToken(String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            return null;
        }
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        try {
            SignatureAlgorithm algorithm = bearerTokenDelegate.getAccessAlgorithm();
            Key verificationKey = bearerTokenDelegate.getAccessVerificationKey(algorithm);
            Claims claims = Jwts.parser().setSigningKey(verificationKey)
                    .parseClaimsJws(accessToken).getBody();
            AccessUserDetails details = bearerTokenDelegate.parseAccessClaims(claims);
            return validateUserDetails(details, null, false) ? details : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    protected void writeResponse(HttpServletResponse response, ResponseCode responseCode, String messageKey) throws IOException {
        if (response == null) {
            return;
        }
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
