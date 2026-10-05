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
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.MediaType;

import jakarta.servlet.http.HttpServletResponse;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Key;
import java.security.PublicKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.cowave.zoo.framework.access.security.AuthMode.ACCESS;
import static com.cowave.zoo.http.client.constants.HttpCode.*;
import static com.cowave.zoo.framework.access.security.AuthMode.ACCESS_REFRESH;

/**
 * @author shanhuiming
 */
@RequiredArgsConstructor
public class BearerTokenServiceImpl implements BearerTokenService {

    /**
     * Refresh令牌（设备）：{applicationName}:auth:refresh:{userAccount}:{deviceId}
     */
    public static final String AUTH_REFRESH_KEY = "%s:auth:refresh:%s:%s";

    /**
     * Access令牌（设备）：{applicationName}:auth:access:{userAccount}:{deviceId}:{tenantCode}:{accessId}
     */
    public static final String AUTH_ACCESS_KEY = "%s:auth:access:%s:%s:%s:%s";

    /**
     * Refresh令牌（应用OAuth）：{applicationName}:auth:oauth:{userAccount}:{tenantCode}:{appId}
     */
    public static final String AUTH_OAUTH_KEY = "%s:auth:oauth:%s:%s:%s";

    /**
     * 账号授权索引，每个账号一个SET，成员使用键序列化器编码，供查询及Lua原子撤销使用
     * key={applicationName}:auth:index:{userAccount}，member=登录Access、设备Refresh或OAuth Refresh的完整Redis键
     */
    public static final String ACCOUNT_TOKEN_INDEX = "%s:auth:index:%s";

    /**
     * 在线登录索引，全局ZSET，成员为userAccount:deviceId，分数为登录时间
     * key={applicationName}:auth:online，member={userAccount}:{deviceId}，score=loginTime
     */
    public static final String ONLINE_INDEX = "%s:auth:online";

    /**
     * 租户在线索引，每租户一个ZSET，成员同样为userAccount:deviceId，分数为登录时间
     * key={applicationName}:auth:online:{tenantCode}，member={userAccount}:{deviceId}，score=loginTime
     */
    public static final String TENANT_ONLINE_INDEX = "%s:auth:online:%s";

    // 在线索引的成员分隔符，解析时取最后一个分隔符
    private static final char MEMBER_SEPARATOR = ':';
    private final RedisHelper redisHelper;
    private final ObjectMapper objectMapper;
    private final AccessIdGenerator accessIdGenerator;
    private final BearerTokenDelegate bearerTokenDelegate;

    @Override
    public void assignAccessToken(AccessUserDetails userDetails) {
        if (StringUtils.isBlank(userDetails.getDeviceId())) {
            userDetails.setDeviceId(IdUtil.fastSimpleUUID());
        }
        // 构造Access令牌
        doAssignAccessToken(userDetails);
        // 启用服务端校验且Redis可用时保存Access记录
        if (userDetails.isAccessStore() && redisHelper != null) {
            String userAccount = userDetails.getUsername();
            String deviceId = userDetails.getDeviceId();
            // 准备Access记录和索引写入
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo(userDetails);
            int accessExpire = bearerTokenDelegate.getAccessExpireSeconds();
            String accessKey = getAccessTokenKey(userDetails);
            TokenMutation mutation = new TokenMutation();
            // 清除过期索引
            mutation.pruneExpiredTokens(userAccount);
            // 单令牌互斥模式，清除该设备历史Access
            if (userDetails.isDeviceLimit()) {
                mutation.removeAccessTokens(userAccount, deviceId);
            }
            // 保存Access令牌，并设置有效期
            mutation.command("SET", accessKey, serialized(accessTokenInfo), raw("EX"), raw(accessExpire));
            // 保存Access索引，方便账号退出时清除Access记录
            mutation.indexToken(userAccount, accessKey);
            // 原子操作
            mutation.executeUnconditionally(accessKey);
        }
    }

    private void doAssignAccessToken(AccessUserDetails userDetails) {
        JwtBuilder jwtBuilder = Jwts.builder();
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
    }

    @Override
    public void assignAccessRefreshToken(AccessUserDetails userDetails) {
        if (StringUtils.isBlank(userDetails.getDeviceId())) {
            userDetails.setDeviceId(IdUtil.fastSimpleUUID());
        }
        // 构造Access令牌
        doAssignAccessToken(userDetails);
        // 构造Refresh令牌
        assignRefreshToken(userDetails);
        if (redisHelper != null) {
            // 如果快照发生变化，则重新读取最新记录再尝试覆盖，最多尝试8次
            for (int attempt = 0; attempt < 8; attempt++) {
                byte[] snapshot = getValueBytes(getRefreshTokenKey(userDetails));
                RefreshTokenInfo preRefresh = deserializeRefresh(snapshot);
                if (replaceLoginTokens(userDetails, preRefresh, snapshot)) {
                    // 如果只允许单设备登录，则撤销其他设备的登录授权
                    if (userDetails.isDeviceLimit()) {
                        Set<String> deviceIds = getLoginDevices(userDetails.getUsername());
                        if (CollectionUtils.isNotEmpty(deviceIds)) {
                            for (String deviceId : new HashSet<>(deviceIds)) {
                                if (!Objects.equals(deviceId, userDetails.getDeviceId())) {
                                    revokeRefreshToken(userDetails.getUsername(), deviceId);
                                }
                            }
                        }
                    }
                    return;
                }
            }
            throw new HttpHintException(CONFLICT, "{frame.auth.refresh.changed}");
        }
    }

    private void assignRefreshToken(AccessUserDetails userDetails) {
        JwtBuilder jwtBuilder = Jwts.builder();
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
    }

    @Override
    public void assignOauthToken(AccessUserDetails userDetails) {
        doAssignOauthToken(userDetails);
        if (redisHelper != null) {
            // 比较快照并覆盖OAuth令牌信息
            String oauthKey = getOauthTokenKey(userDetails.getUsername(), userDetails.getTenantCode(), userDetails.getOauthId());
            for (int attempt = 0; attempt < 8; attempt++) {
                byte[] snapshot = getValueBytes(oauthKey);
                if (replaceOauthTokens(userDetails, snapshot)) {
                    return;
                }
            }
            throw new HttpHintException(CONFLICT, "{frame.auth.refresh.changed}");
        }
    }

    private void doAssignOauthToken(AccessUserDetails userDetails) {
        // 检查授权来源设备的登录状态
        validateOauthDevice(userDetails);
        // 不保存OAuth Access记录
        userDetails.setAccessStore(false);
        // 构造OAuth Access令牌
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
        // 构造OAuth Refresh令牌
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
    }

    private void validateOauthDevice(AccessUserDetails userDetails) {
        if (redisHelper == null || StringUtils.isBlank(userDetails.getDeviceId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.oauth.device.offline}");
        }
        RefreshTokenInfo deviceRefreshToken = redisHelper.getValue(getRefreshTokenKey(userDetails));
        if (deviceRefreshToken == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.oauth.device.offline}");
        }
    }

    @Override
    public String refreshAccessToken() throws Exception {
        AccessUserDetails userDetails = parseAccessToken(null);
        // 刷新替换直接删除旧Access及账号索引成员，不保留撤销标记
        if (userDetails.isAccessStore() && redisHelper != null) {
            String accessKey = getAccessTokenKey(userDetails);
            TokenMutation mutation = new TokenMutation();
            mutation.removeToken(userDetails.getUsername(), accessKey);
            mutation.executeUnconditionally(accessKey);
        }
        // 构造新的Access令牌
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
            SignatureAlgorithm algorithm = bearerTokenDelegate.getRefreshAlgorithm();
            Key verificationKey = bearerTokenDelegate.getRefreshVerificationKey(algorithm);
            claims = parseSignedClaims(refreshToken, algorithm, verificationKey);
        } catch (Exception e) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.invalid}");
        }

        AccessUserDetails details = bearerTokenDelegate.parseRefreshClaims(claims);
        // 获取旧的Refresh令牌
        String refreshTokenKey = getRefreshTokenKey(details.getUsername(), details.getDeviceId());
        byte[] snapshot = getValueBytes(refreshTokenKey);
        RefreshTokenInfo preRefresh = deserializeRefresh(snapshot);
        if (preRefresh == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 校验refreshId，拒绝已被刷新或重新登录替换的旧版本
        if (!Objects.equals(details.getRefreshId(), preRefresh.getRefreshId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }

        // 业务扩展，重新加载用户、租户及权限信息
        AccessUserDetails userDetails = new AccessUserDetails(preRefresh);
        reloadRefreshUserDetails(userDetails);

        // 更新Access/Refresh版本，更新访问时间和IP，保留原登录时间
        userDetails.setAccessId(IdUtil.fastSimpleUUID());
        userDetails.setRefreshId(IdUtil.fastSimpleUUID());
        userDetails.setAccessIp(Access.accessIp());
        userDetails.setAccessTime(Access.accessTime());
        // 构造Access令牌
        doAssignAccessToken(userDetails);
        // 构造Refresh令牌
        assignRefreshToken(userDetails);
        // 刷新时如果发现快照发生变化，则放弃覆盖更新
        if (!replaceLoginTokens(userDetails, preRefresh, snapshot)) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }
        return userDetails;
    }

    protected void reloadRefreshUserDetails(AccessUserDetails userDetails) {

    }

    @Override
    public AccessUserDetails refreshOauthToken(String oauthToken) {
        assert redisHelper != null;
        Claims claims;
        try {
            SignatureAlgorithm algorithm = bearerTokenDelegate.getRefreshAlgorithm();
            Key verificationKey = bearerTokenDelegate.getRefreshVerificationKey(algorithm);
            claims = parseSignedClaims(oauthToken, algorithm, verificationKey);
        } catch (Exception e) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.invalid}");
        }

        AccessUserDetails details = bearerTokenDelegate.parseOauthRefreshClaims(claims);
        // 获取旧的OAuth Refresh令牌
        String oauthTokenKey = getOauthTokenKey(details.getUsername(), details.getTenantCode(), details.getOauthId());
        byte[] snapshot = getValueBytes(oauthTokenKey);
        RefreshTokenInfo preRefresh = deserializeRefresh(snapshot);
        if (preRefresh == null) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.empty}");
        }

        // 校验refreshId，拒绝已被刷新或重新授权替换的旧版本
        if (!Objects.equals(details.getRefreshId(), preRefresh.getRefreshId())) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }

        // 更新授权id，访问时间和IP（不重新加载管理端权限）
        AccessUserDetails userDetails = new AccessUserDetails(preRefresh);
        userDetails.setAccessId(IdUtil.fastSimpleUUID());
        userDetails.setRefreshId(IdUtil.fastSimpleUUID());
        userDetails.setAccessIp(Access.accessIp());
        userDetails.setAccessTime(Access.accessTime());
        // 构造OAuth Access/Refresh令牌
        doAssignOauthToken(userDetails);
        // 刷新时如果发现快照发生变化，则放弃覆盖更新
        if (!replaceOauthTokens(userDetails, snapshot)) {
            throw new HttpHintException(UNAUTHORIZED, "{frame.auth.refresh.changed}");
        }
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
            claims = parseSignedClaims(accessToken, algorithm, verificationKey);
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

    protected boolean validateUserDetails(AccessUserDetails userDetails,
                                          HttpServletResponse response, boolean useRefreshToken) throws IOException {
        // OAuth场景，服务端Redis不校验Access令牌
        if (StringUtils.isNotBlank(userDetails.getOauthId())) {
            return true;
        }

        // 不进行服务端Redis校验Access令牌
        if (!userDetails.isAccessStore()) {
            return true;
        }

        if (useRefreshToken) {
            // Refresh Token已不存在 401
            RefreshTokenInfo refreshTokenInfo = redisHelper.getValue(getRefreshTokenKey(userDetails));
            if (refreshTokenInfo == null) {
                writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                return false;
            }
            // Refresh Token已被覆盖 498
            if (!Objects.equals(userDetails.getRefreshId(), refreshTokenInfo.getRefreshId())) {
                writeResponse(response, INVALID_TOKEN, "frame.auth.access.replaced");
                return false;
            }

            AccessTokenInfo accessTokenInfo = redisHelper.getValue(getAccessTokenKey(userDetails));
            // Access Token已被删除或被撤销
            if (accessTokenInfo == null || accessTokenInfo.getRevoked() == 1) {
                // 发现刷新或重新登录导致版本变化时返回498
                RefreshTokenInfo latest = redisHelper.getValue(getRefreshTokenKey(userDetails));
                if (latest != null && !Objects.equals(userDetails.getRefreshId(), latest.getRefreshId())) {
                    writeResponse(response, INVALID_TOKEN, "frame.auth.access.replaced");
                    return false;
                }
                // Refresh没有变化，Access已不存在或标记撤销，返回401
                writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                return false;
            }
        } else {
            // 3. 单令牌模式只检查Access记录是否存在且未标记撤销
            AccessTokenInfo accessTokenInfo = redisHelper.getValue(getAccessTokenKey(userDetails));
            if (accessTokenInfo == null || accessTokenInfo.getRevoked() == 1) {
                writeResponse(response, UNAUTHORIZED, "frame.auth.access.revoked");
                return false;
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
            claims = parseSignedClaims(accessToken, algorithm, verificationKey);
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

        // 单令牌模式以请求Access的现存快照为条件，撤销账号全部登录及OAuth授权
        if (ACCESS == bearerTokenDelegate.authMode()) {
            String accessKey = getAccessTokenKey(userDetails);
            byte[] snapshot = getValueBytes(accessKey);
            if (snapshot != null) {
                deleteAccountTokens(userDetails.getUsername(), accessKey, snapshot);
            }
        }

        // 双令牌模式使用Access中的refreshId比较当前设备版本，客户端不用再传Refresh或refreshId
        if (ACCESS_REFRESH == bearerTokenDelegate.authMode()) {
            String refreshKey = getRefreshTokenKey(userDetails);
            byte[] snapshot = getValueBytes(refreshKey);
            RefreshTokenInfo refreshTokenInfo = deserializeRefresh(snapshot);
            if (refreshTokenInfo == null) {
                return;
            }

            // 快照发生变化
            if (!Objects.equals(userDetails.getRefreshId(), refreshTokenInfo.getRefreshId())
                    || !deleteAccountTokens(userDetails.getUsername(), refreshKey, snapshot)) {
                RefreshTokenInfo latest = redisHelper.getValue(refreshKey);
                if (latest != null) {
                    // latest是版本已被刷新或重新登录替换，那么返回498供客户端刷新后重试退出
                    throw new HttpHintException(INVALID_TOKEN, "{frame.auth.access.replaced}");
                }
            }
        }
    }

    @Override
    public AccessTokenInfo revokeAccessToken(String userAccount, String deviceId, String tenantCode, String accessId) {
        String accessKey = getAccessTokenKey(userAccount, deviceId, tenantCode, accessId);
        for (int attempt = 0; attempt < 8; attempt++) {
            // 读取指定Access快照，已不存在或已标记撤销时无需重复处理
            byte[] snapshot = getValueBytes(accessKey);
            AccessTokenInfo accessTokenInfo = deserializeAccess(snapshot);
            if (accessTokenInfo == null || accessTokenInfo.getRevoked() == 1) {
                return accessTokenInfo;
            }
            // 标记撤销并保留剩余TTL及账号索引成员
            accessTokenInfo.setRevoked(1);
            TokenMutation mutation = new TokenMutation();
            mutation.command("SET", accessKey, serialized(accessTokenInfo), raw("KEEPTTL"));
            // 快照匹配才更新，避免刷新或退出已经删除记录后又把旧Access写回来
            if (mutation.execute(accessKey, snapshot)) {
                return accessTokenInfo;
            }
        }
        throw new HttpHintException(CONFLICT, "{frame.auth.access.replaced}");
    }

    @Override
    public RefreshTokenInfo revokeRefreshToken(String userAccount, String deviceId) {
        // 管理员踢设备或单设备登录互斥场景
        String refreshKey = getRefreshTokenKey(userAccount, deviceId);
        for (int attempt = 0; attempt < 8; attempt++) {
            byte[] snapshot = getValueBytes(refreshKey);
            RefreshTokenInfo refreshTokenInfo = deserializeRefresh(snapshot);
            // 比较快照，删除设备登录及Access
            if (deleteLoginTokens(userAccount, deviceId, refreshTokenInfo, snapshot)) {
                return refreshTokenInfo;
            }
        }
        throw new HttpHintException(CONFLICT, "{frame.auth.refresh.changed}");
    }

    @Override
    public void revokeUserTokens(String userAccount) {
        accountTokenDeletion(userAccount).executeUnconditionally(getAccountTokenIndexKey(userAccount));
    }

    @Override
    public RefreshTokenInfo revokeOauthToken(String userAccount, String tenantCode, String appId) {
        String oauthKey = getOauthTokenKey(userAccount, tenantCode, appId);
        for (int attempt = 0; attempt < 8; attempt++) {
            byte[] snapshot = getValueBytes(oauthKey);
            TokenMutation mutation = new TokenMutation();
            // 删除指定应用的OAuth令牌，以及索引
            mutation.removeToken(userAccount, oauthKey);
            // 比较快照并执行
            if (mutation.execute(oauthKey, snapshot)) {
                return deserializeRefresh(snapshot);
            }
        }
        throw new HttpHintException(CONFLICT, "{frame.auth.refresh.changed}");
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
            refreshKeys.add(getRefreshTokenKey(index.getUserAccount(), index.getDeviceId()));
        }

        List<RefreshTokenInfo> refreshTokenList = redisHelper.getMultiValue(refreshKeys);
        if (CollectionUtils.isEmpty(refreshTokenList)) {
            return onlineTokens;
        }

        List<OnlineIndex> livedList = new ArrayList<>();
        for (int i = 0; i < indexList.size(); i++) {
            OnlineIndex index = indexList.get(i);
            RefreshTokenInfo refreshToken = refreshTokenList.get(i);
            // Refresh记录已过期或已撤销，但在线索引成员尚未清理
            if (refreshToken == null) {
                removeExpiredOnlineIndex(index);
                continue;
            }
            if (StringUtils.isNotBlank(index.getTenantCode())
                    && !Objects.equals(index.getTenantCode(), refreshToken.getTenantCode())) {
                unIndexTenantOnline(index.getTenantCode(), index.getUserAccount(), index.getDeviceId());
                continue;
            }
            index.setTenantCode(refreshToken.getTenantCode());
            livedList.add(index);
            onlineTokens.add(new OnlineToken(refreshToken));
        }

        // 补充令牌信息
        if (!livedList.isEmpty()) {
            fillGrantToken(livedList, onlineTokens);
        }
        return onlineTokens;
    }

    private void fillGrantToken(List<OnlineIndex> indexList, List<OnlineToken> onlineTokens) {
        List<OnlineRecord> records = new ArrayList<>();
        Set<String> oauthAccounts = new HashSet<>();
        Map<String, Set<String>> accountKeys = new HashMap<>();
        for (int i = 0; i < indexList.size(); i++) {
            OnlineIndex index = indexList.get(i);
            /*
             * 1. 每个账号只读取一次总索引，从完整键筛选设备Access和账号OAuth Refresh。
             *    同账号、同在线租户上下文只展示一次OAuth列表，应用授权不归属某台设备。
             */
            Set<String> tokenKeys = accountKeys.computeIfAbsent(index.getUserAccount(), this::getAccountTokenKeys);
            boolean includeOauth = oauthAccounts.add(index.getUserAccount() + MEMBER_SEPARATOR + index.getTenantCode());
            String accessPrefix = getAccessTokenPrefix(index.getUserAccount(), index.getDeviceId());
            String oauthPrefix = getOauthTokenPrefix(index.getUserAccount());
            for (String tokenKey : tokenKeys) {
                if (tokenKey.startsWith(accessPrefix) || (includeOauth && tokenKey.startsWith(oauthPrefix))) {
                    records.add(new OnlineRecord(i, index.getUserAccount(), tokenKey));
                }
            }
        }
        if (records.isEmpty()) {
            return;
        }

        // 2. 批量读取实际授权记录，再分别填充设备Access和账号OAuth列表
        List<String> tokenKeys = new ArrayList<>(records.size());
        for (OnlineRecord record : records) {
            tokenKeys.add(record.tokenKey());
        }
        List<Object> tokens = redisHelper.getMultiValue(tokenKeys);
        if (CollectionUtils.isEmpty(tokens)) {
            return;
        }

        Set<String> expiredAccounts = new HashSet<>();
        for (int i = 0; i < records.size(); i++) {
            OnlineRecord record = records.get(i);
            Object token = tokens.get(i);
            if (token == null) {
                // 索引读取后记录可能过期；后面以Lua重新检查存在性，避免误移除刚重新签发的同名键
                expiredAccounts.add(record.userAccount());
                continue;
            }

            OnlineToken onlineToken = onlineTokens.get(record.ownerIndex());
            if (token instanceof AccessTokenInfo accessToken) {
                onlineToken.getAccessTokens().add(accessToken);
            } else {
                onlineToken.getOauthTokens().add((RefreshTokenInfo) token);
            }
        }
        // 3. 清理账号总索引中已不存在的令牌键，不删除有效授权记录
        expiredAccounts.forEach(this::pruneAccountTokenIndex);
    }

    // 清除失效索引
    private void removeExpiredOnlineIndex(OnlineIndex index) {
        TokenMutation mutation = new TokenMutation();
        String member = onlineMember(index.getUserAccount(), index.getDeviceId());
        // 移除失效的在线索引
        mutation.command("ZREM", getOnlineIndexKey(), serialized(member));
        if (StringUtils.isNotBlank(index.getTenantCode())) {
            // 移除失效的租户在线索引
            mutation.command("ZREM", getTenantOnlineIndexKey(index.getTenantCode()), serialized(member));
        }
        // 移除令牌索引中的失效Refresh键；不删除该设备可能仍有效的纯Access记录
        String refreshKey = getRefreshTokenKey(index.getUserAccount(), index.getDeviceId());
        mutation.command("SREM", getAccountTokenIndexKey(index.getUserAccount()), serializedKey(refreshKey));
        mutation.execute(refreshKey, null);
    }

    private void unIndexTenantOnline(String tenantCode, String userAccount, String deviceId) {
        if (StringUtils.isNotBlank(tenantCode)) {
            // 重读设备当前快照，旧查询不能移除并发切回该租户的新登录成员
            String refreshKey = getRefreshTokenKey(userAccount, deviceId);
            byte[] currentValue = getValueBytes(refreshKey);
            RefreshTokenInfo current = deserializeRefresh(currentValue);
            if (current != null && Objects.equals(tenantCode, current.getTenantCode())) {
                return;
            }
            TokenMutation mutation = new TokenMutation();
            mutation.command("ZREM", getTenantOnlineIndexKey(tenantCode), serialized(onlineMember(userAccount, deviceId)));
            mutation.execute(refreshKey, currentValue);
        }
    }

    // 获取账号令牌索引（先清理已过期或已撤销的成员）
    private Set<String> getAccountTokenKeys(String userAccount) {
        pruneAccountTokenIndex(userAccount);
        return getKeySet(getAccountTokenIndexKey(userAccount));
    }

    // 清除过期索引键
    private void pruneAccountTokenIndex(String userAccount) {
        TokenMutation mutation = new TokenMutation();
        mutation.pruneExpiredTokens(userAccount);
        mutation.executeUnconditionally(getAccountTokenIndexKey(userAccount));
    }

    // 根据索引获取登录设备
    private Set<String> getLoginDevices(String userAccount) {
        String refreshPrefix = getRefreshTokenKey(userAccount, "");
        Set<String> devices = new HashSet<>();
        for (String tokenKey : getAccountTokenKeys(userAccount)) {
            if (tokenKey.startsWith(refreshPrefix)) {
                devices.add(tokenKey.substring(refreshPrefix.length()));
            }
        }
        return devices;
    }

    // 指定账号、设备的Access前缀
    private String getAccessTokenPrefix(String userAccount, String deviceId) {
        return "%s:auth:access:%s:%s:".formatted(bearerTokenDelegate.getAccessIssuer(), userAccount, deviceId);
    }

    // 指定账号的OAuth Refresh前缀
    private String getOauthTokenPrefix(String userAccount) {
        return "%s:auth:oauth:%s:".formatted(bearerTokenDelegate.getRefreshIssuer(), userAccount);
    }

    private static String onlineMember(String userAccount, String deviceId) {
        return userAccount + MEMBER_SEPARATOR + deviceId;
    }

    private String getOnlineIndexKey() {
        return ONLINE_INDEX.formatted(bearerTokenDelegate.getRefreshIssuer());
    }

    private String getTenantOnlineIndexKey(String tenantCode) {
        return TENANT_ONLINE_INDEX.formatted(bearerTokenDelegate.getRefreshIssuer(), tenantCode);
    }

    private String getAccessTokenKey(AccessUserDetails userDetails) {
        return getAccessTokenKey(userDetails.getUsername(), userDetails.getDeviceId(),
                userDetails.getTenantCode(), userDetails.getAccessId());
    }

    private String getAccessTokenKey(String userAccount, String deviceId,
                                     String tenantCode, String accessId) {
        return AUTH_ACCESS_KEY.formatted(bearerTokenDelegate.getAccessIssuer(),
                userAccount, deviceId, tenantCode, accessId);
    }

    private String getRefreshTokenKey(AccessUserDetails userDetails) {
        return getRefreshTokenKey(userDetails.getUsername(), userDetails.getDeviceId());
    }

    private String getRefreshTokenKey(String userAccount, String deviceId) {
        return AUTH_REFRESH_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(), userAccount, deviceId);
    }

    private String getOauthTokenKey(String userAccount, String tenantCode, String appId) {
        return AUTH_OAUTH_KEY.formatted(bearerTokenDelegate.getRefreshIssuer(),
                userAccount, tenantCode, appId);
    }

    private String getAccountTokenIndexKey(String account) {
        return ACCOUNT_TOKEN_INDEX.formatted(bearerTokenDelegate.getRefreshIssuer(), account);
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
            JwtParserBuilder parser = Jwts.parser();
            if (algorithm.name().startsWith("HS")) {
                parser.verifyWith((SecretKey) verificationKey);
            } else {
                parser.verifyWith((PublicKey) verificationKey);
            }
            // 此方法仅验证JWT，不通过登录或OAuth Refresh记录判断Access是否仍有效
            parser.build().parseSignedClaims(accessToken).getPayload();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private Claims parseSignedClaims(String token, SignatureAlgorithm algorithm, Key verificationKey) {
        JwtParserBuilder parser = Jwts.parser();
        if (algorithm.name().startsWith("HS")) {
            parser.verifyWith((SecretKey) verificationKey);
        } else {
            parser.verifyWith((PublicKey) verificationKey);
        }
        return parser.build().parseSignedClaims(token).getPayload();
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
            JwtParserBuilder parser = Jwts.parser();
            if (algorithm.name().startsWith("HS")) {
                parser.verifyWith((SecretKey) verificationKey);
            } else {
                parser.verifyWith((PublicKey) verificationKey);
            }
            Claims claims = parser.build().parseSignedClaims(accessToken).getPayload();
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

    private boolean replaceLoginTokens(AccessUserDetails details, RefreshTokenInfo preRefresh, byte[] snapshot) {
        String account = details.getUsername();
        String device = details.getDeviceId();
        TokenMutation mutation = new TokenMutation();
        // 清理索引过期成员
        mutation.pruneExpiredTokens(account);
        // 索引筛选该设备全部旧Access并删除
        mutation.removeAccessTokens(account, device);
        // 账号切换租户时，从旧租户的在线索引中移除
        if (preRefresh != null && StringUtils.isNotBlank(preRefresh.getTenantCode())
                && !Objects.equals(preRefresh.getTenantCode(), details.getTenantCode())) {
            mutation.command("ZREM", getTenantOnlineIndexKey(preRefresh.getTenantCode()), serialized(onlineMember(account, device)));
        }

        // 保存新的Refresh令牌（覆盖），并设置有效期
        int refreshExpire = bearerTokenDelegate.getRefreshExpireSeconds();
        RefreshTokenInfo newRefresh = new RefreshTokenInfo(details);
        long loginTimestamp = newRefresh.getLoginTime() == null ? 0L : newRefresh.getLoginTime().getTime();
        mutation.command("SET", getRefreshTokenKey(details), serialized(newRefresh), raw("EX"), raw(refreshExpire));
        // 保存新的Refresh令牌索引
        mutation.indexToken(account, getRefreshTokenKey(details));
        // 需要服务端校验Access时，保存新Access令牌，以及索引设置
        if (details.isAccessStore()) {
            mutation.command("SET", getAccessTokenKey(details), serialized(new AccessTokenInfo(details)),
                    raw("EX"), raw(bearerTokenDelegate.getAccessExpireSeconds()));
            mutation.indexToken(account, getAccessTokenKey(details));
        }

        // 更新在线索引
        mutation.command("ZADD", getOnlineIndexKey(), raw(loginTimestamp), serialized(onlineMember(account, device)));
        // 续期在线索引（单个成员是否有效需查询其Refresh记录判断）
        mutation.command("EXPIRE", getOnlineIndexKey(), raw(refreshExpire));
        if (StringUtils.isNotBlank(details.getTenantCode())) {
            // 更新租户在线索引
            mutation.command("ZADD", getTenantOnlineIndexKey(details.getTenantCode()), raw(loginTimestamp),
                    serialized(onlineMember(account, device)));
            // 续期租户在线索引
            mutation.command("EXPIRE", getTenantOnlineIndexKey(details.getTenantCode()), raw(refreshExpire));
        }
        // 只在快照没有发生变化时，才提交全部变更
        return mutation.execute(getRefreshTokenKey(details), snapshot);
    }

    // 删除指定设备的登录及Access，不删除OAuth记录；来源设备离线后OAuth刷新会被拒绝
    private boolean deleteLoginTokens(String account, String device, RefreshTokenInfo current, byte[] currentValue) {
        TokenMutation mutation = new TokenMutation();
        // 删除设备Refresh令牌，及其索引
        mutation.removeToken(account, getRefreshTokenKey(account, device));
        // 删除设备全部Access及对应索引
        mutation.removeAccessTokens(account, device);
        // 从在线索引中移除
        mutation.command("ZREM", getOnlineIndexKey(), serialized(onlineMember(account, device)));
        if (current != null && StringUtils.isNotBlank(current.getTenantCode())) {
            // 从租户在线索引中移除
            mutation.command("ZREM", getTenantOnlineIndexKey(current.getTenantCode()), serialized(onlineMember(account, device)));
        }
        // 只在快照匹配时才执行
        return mutation.execute(getRefreshTokenKey(account, device), currentValue);
    }

    private boolean replaceOauthTokens(AccessUserDetails details, byte[] snapshot) {
        String account = details.getUsername();
        String oauthKey = getOauthTokenKey(account, details.getTenantCode(), details.getOauthId());
        TokenMutation mutation = new TokenMutation();
        // 清除过期索引
        mutation.pruneExpiredTokens(account);
        // 构造保存新的OAuth Refresh
        int expire = bearerTokenDelegate.getRefreshExpireSeconds();
        RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(details);
        refreshTokenInfo.setAccessId(null);
        mutation.command("SET", oauthKey, serialized(refreshTokenInfo), raw("EX"), raw(expire));
        // 保存索引
        mutation.indexToken(account, oauthKey);
        // 只在快照匹配时才执行
        return mutation.execute(oauthKey, snapshot);
    }

    // 清除账号所有设备的Refresh/Access令牌，应用OAuth Refresh，以及对应的索引
    private boolean deleteAccountTokens(String account, String compareKey, byte[] expected) {
        return accountTokenDeletion(account).execute(compareKey, expected);
    }

    private TokenMutation accountTokenDeletion(String account) {
        TokenMutation mutation = new TokenMutation();
        Set<String> devices = getLoginDevices(account);
        for (String device : devices) {
            RefreshTokenInfo deviceRefreshToken = redisHelper.getValue(getRefreshTokenKey(account, device));
            // 从在线索引中移除
            mutation.command("ZREM", getOnlineIndexKey(), serialized(onlineMember(account, device)));
            if (deviceRefreshToken != null && StringUtils.isNotBlank(deviceRefreshToken.getTenantCode())) {
                // 从租户在线索引中移除
                mutation.command("ZREM", getTenantOnlineIndexKey(deviceRefreshToken.getTenantCode()), serialized(onlineMember(account, device)));
            }
        }
        // 删除账号令牌及索引
        mutation.deleteAccountIndex(account);
        return mutation;
    }

    private byte[] getValueBytes(String key) {
        return (byte[]) redisHelper.getRedisTemplate().execute((RedisCallback<byte[]>) connection ->
                connection.stringCommands().get(serializedKey(key)));
    }

    // 读取账号令牌索引，成员保存完整Redis键，使用键序列化器解码
    @SuppressWarnings("unchecked")
    private Set<String> getKeySet(String key) {
        Set<byte[]> members = (Set<byte[]>) redisHelper.getRedisTemplate().execute((RedisCallback<Set<byte[]>>) connection ->
                connection.setCommands().sMembers(serializedKey(key)));
        Set<String> tokenKeys = new HashSet<>();
        if (members != null) {
            for (byte[] member : members) {
                tokenKeys.add((String) redisHelper.getKeySerializer().deserialize(member));
            }
        }
        return tokenKeys;
    }

    // 编码Redis键及账号索引中保存的完整令牌键，与普通记录值的编码分开
    private byte[] serializedKey(String key) {
        return redisHelper.getKeySerializer().serialize(key);
    }

    // 编码令牌记录或在线索引成员，沿用Redis配置的值序列化器
    private byte[] serialized(Object value) {
        return redisHelper.getValueSerializer().serialize(value);
    }

    private AccessTokenInfo deserializeAccess(byte[] value) {
        return value == null ? null : (AccessTokenInfo) redisHelper.getValueSerializer().deserialize(value);
    }

    // 从同一份原始快照解析Refresh信息，原始字节仍用于条件提交
    private RefreshTokenInfo deserializeRefresh(byte[] value) {
        return value == null ? null : (RefreshTokenInfo) redisHelper.getValueSerializer().deserialize(value);
    }

    // 编码Lua及Redis命令使用的文本参数，不使用对象值序列化器
    private byte[] raw(Object value) {
        return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }

    private class TokenMutation {

        private final StringBuilder script = new StringBuilder();

        private final List<String> keys = new ArrayList<>();

        private final List<byte[]> arguments = new ArrayList<>();

        // 登记账号的令牌索引（包括：Access、Refresh、OAuth Refresh）
        private void indexToken(String account, String tokenKey) {
            String indexKey = getAccountTokenIndexKey(account);
            command("SADD", indexKey, serializedKey(tokenKey));
            // 取较长的配置有效期来续期索引，避免索引先于本次写入的令牌失效
            int expire = Math.max(bearerTokenDelegate.getRefreshExpireSeconds(), bearerTokenDelegate.getAccessExpireSeconds());
            command("EXPIRE", indexKey, raw(expire));
        }

        // 删除令牌，并从账号令牌索引中移除
        private void removeToken(String account, String tokenKey) {
            // 删除令牌
            command("DEL", tokenKey);
            // 删除索引
            command("SREM", getAccountTokenIndexKey(account), serializedKey(tokenKey));
        }

        // 删除指定设备的全部Access，以及对应索引记录（场景：登录、刷新、撤销）
        private void removeAccessTokens(String account, String device) {
            keys.add(getAccountTokenIndexKey(account));
            int keyIndex = keys.size() + 1;
            arguments.add(serializedKey(getAccessTokenPrefix(account, device)));
            int prefixIndex = arguments.size() + 2;
            // 枚举全部成员，匹配前缀进行删除
            script.append("for _, tokenKey in ipairs(redis.call('smembers', KEYS[").append(keyIndex)
                    .append("])) do if string.sub(tokenKey, 1, string.len(ARGV[").append(prefixIndex)
                    .append("])) == ARGV[").append(prefixIndex).append("] then redis.call('del', tokenKey); ")
                    .append("redis.call('srem', KEYS[").append(keyIndex).append("], tokenKey) end end; ");
        }


        // 清除账号已过期的索引键（对应令牌已不存在）
        private void pruneExpiredTokens(String account) {
            keys.add(getAccountTokenIndexKey(account));
            int index = keys.size() + 1;
            // 记录不存在执行SREM
            script.append("for _, tokenKey in ipairs(redis.call('smembers', KEYS[").append(index)
                    .append("])) do if redis.call('exists', tokenKey) == 0 then redis.call('srem', KEYS[")
                    .append(index).append("], tokenKey) end end; ");
        }

        // 删除账号的令牌以及索引
        private void deleteAccountIndex(String account) {
            keys.add(getAccountTokenIndexKey(account));
            int index = keys.size() + 1;
            // 删除账号令牌
            script.append("for _, tokenKey in ipairs(redis.call('smembers', KEYS[").append(index)
                    .append("])) do redis.call('del', tokenKey) end; ");
            // 删除账号令牌索引本身
            command("DEL", getAccountTokenIndexKey(account));
        }

        // 追加关联键变更命令
        private void command(String command, String key, byte[]... values) {
            // 1. 将关联键追加到KEYS，跳过KEYS[1]的条件比较键
            keys.add(key);
            script.append("redis.call('").append(command).append("', KEYS[").append(keys.size() + 1).append(']');
            // 2. 将已编码参数追加到ARGV，跳过ARGV[1]的比较模式和ARGV[2]的期望字节值
            for (byte[] value : values) {
                arguments.add(value);
                script.append(", ARGV[").append(arguments.size() + 2).append(']');
            }
            script.append("); ");
        }

        // 无条件提交执行
        private void executeUnconditionally(String key) {
            execute(key, null, false);
        }

        // 只在快照没有变化时提交执行
        private boolean execute(String compareKey, byte[] expected) {
            return execute(compareKey, expected, true);
        }

        // 在同一Lua脚本内校验令牌快照并执行全部记录及索引变更，避免比较和写入之间发生并发轮换
        private boolean execute(String compareKey, byte[] expected, boolean compare) {
            // 1. 条件模式先检查记录不存在或原始字节一致，无条件模式跳过比较
            String luaScript = "if ARGV[1] == '0' then "
                    + "if redis.call('exists', KEYS[1]) ~= 0 then return 0 end "
                    + "elseif ARGV[1] == '1' and redis.call('get', KEYS[1]) ~= ARGV[2] then return 0 end "
                    + script + " return 1";
            // 2. KEYS[1]保存条件比较键，后续关联键顺序与命令组装时的偏移保持一致
            List<byte[]> parameters = new ArrayList<>();
            parameters.add(serializedKey(compareKey));
            for (String relatedKey : keys) {
                parameters.add(serializedKey(relatedKey));
            }
            // 3. ARGV[1]保存比较模式，ARGV[2]保存原始快照，后续为已编码的变更参数
            parameters.add(raw(compare ? (expected == null ? "0" : "1") : "2"));
            parameters.add(expected == null ? new byte[0] : expected);
            parameters.addAll(arguments);
            // 4. 原子执行；条件不满足返回0且不执行变更，全部命令执行完成返回1
            Long changed = (Long) redisHelper.getRedisTemplate().execute((RedisCallback<Long>) connection ->
                    connection.scriptingCommands().eval(raw(luaScript), ReturnType.INTEGER,
                            keys.size() + 1, parameters.toArray(byte[][]::new)));
            return Long.valueOf(1).equals(changed);
        }
    }
}
