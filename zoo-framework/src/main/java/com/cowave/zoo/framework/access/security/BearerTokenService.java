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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    AccessTokenInfo revokeAccessToken(String tenantId, String authType, String userAccount, String accessId);

    /**
     * 注销RefreshToken
     */
    RefreshTokenInfo revokeRefreshToken(String tenantId, String authType, String userAccount);

    /**
     * 注销OAuthToken
     */
    RefreshTokenInfo revokeOauthToken(String tenantId, String authType, String userAccount, String appId);

    /**
     * 验证AccessToken
     */
    boolean validAccessToken(String accessToken);

    /**
     * 获取AccessToken列表
     */
    List<AccessTokenInfo> listAccessToken(String tenantId);

    /**
     * 获取RefreshTokenInfo列表
     */
    List<RefreshTokenInfo> listRefreshToken(String tenantId);

    /**
     * 获取OAuthToken列表
     */
    List<RefreshTokenInfo> listOauthToken(String tenantId);
}
