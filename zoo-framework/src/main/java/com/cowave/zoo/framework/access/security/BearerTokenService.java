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

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author shanhuiming
 */
public interface BearerTokenService {

    /**
     * BearerTokenFilter解析AccessToken
     */
    AccessUserDetails parseAccessToken(HttpServletResponse response) throws IOException;

    /**
     * BearerTokenFilter解析AccessToken（使用RefreshToken）
     */
    AccessUserDetails parseAccessRefreshToken(HttpServletResponse response) throws IOException;

    /**
     * 授权设置AccessToken
     */
    void assignAccessToken(AccessUserDetails userDetails);

    /**
     * 授权设置AccessToken和RefreshToken
     */
    void assignAccessRefreshToken(AccessUserDetails userDetails);

    /**
     * 授权应用OAuthToken
     */
    void assignOauthToken(AccessUserDetails userDetails);

    /**
     * 刷新AccessToken
     */
    String refreshAccessToken() throws Exception;

    /**
     * 刷新AccessToken和RefreshToken
     */
    AccessUserDetails refreshAccessRefreshToken(String refreshToken);

    /**
     * 刷新OAuthToken
     */
    AccessUserDetails refreshOauthToken(String oauthToken);

    /**
     * 注销
     */
    void revoke();

    /**
     * 注销AccessToken
     */
    AccessTokenInfo revokeAccessToken(String userAccount, String deviceId, String tenantCode, String accessId);

    /**
     * 注销RefreshToken
     */
    RefreshTokenInfo revokeRefreshToken(String userAccount, String deviceId);

    /**
     * 账号级撤销，比如修改密码后下线全部设备及OAuth授权
     */
    void revokeUserTokens(String userAccount);

    /**
     * 注销指定账号、租户、应用的OAuth Refresh，不影响已签发Access的有效期
     */
    RefreshTokenInfo revokeOauthToken(String userAccount, String tenantCode, String appId);

    /**
     * 验证Access JWT签名及有效期，不查询Redis令牌记录
     */
    boolean validAccessToken(String accessToken);

    /**
     *  校验Socket连接Access Token
     */
    AccessUserDetails validateSocketAccessToken(String accessToken);

    /**
     * 在线用户索引
     */
    List<OnlineIndex> listOnlineIndex(Date beginTime, Date endTime);

    /**
     * 指定租户在线用户索引
     */
    List<OnlineIndex> listTenantOnlineIndex(String tenantCode, Date beginTime, Date endTime);

    /**
     * 在线用户令牌
     */
    List<OnlineToken> listOnlineToken(List<OnlineIndex> members);
}
