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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author shanhuiming
 */
public interface BearerTokenService {
    String CLAIM_TYPE = "Token.type";
    String CLAIM_ACCESS_IP = "Token.ip";
    String CLAIM_ACCESS_ID = "Token.access";
    String CLAIM_REFRESH_ID = "Token.refresh";
    String CLAIM_CONFLICT = "Token.conflict";
    String CLAIM_USER_ID = "User.id";
    String CLAIM_USER_CODE = "User.code";
    String CLAIM_USER_PROPERTIES = "User.properties";
    String CLAIM_USER_NAME = "User.name";
    String CLAIM_USER_ACCOUNT = "User.account";
    String CLAIM_USER_ROLE = "User.role";
    String CLAIM_USER_PERM = "User.permission";
    String CLAIM_DEPT_ID = "Dept.id";
    String CLAIM_DEPT_CODE = "Dept.code";
    String CLAIM_DEPT_NAME = "Dept.name";
    String CLAIM_CLUSTER_ID = "Cluster.id";
    String CLAIM_CLUSTER_LEVEL = "Cluster.level";
    String CLAIM_CLUSTER_NAME = "Cluster.name";

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
     * 刷新AccessToken
     */
    String refreshAccessToken() throws Exception;

    /**
     * 刷新AccessToken和RefreshToken
     */
    AccessUserDetails refreshAccessRefreshToken(String refreshToken);

    /**
     * 注销AccessToken
     */
    AccessTokenInfo revokeAccessToken(String accessId);

    /**
     * 注销AccessToken和RefreshToken
     */
    RefreshTokenInfo revokeAccessRefreshToken();

    /**
     * 验证AccessToken
     */
    boolean validAccessToken(String accessToken);

    /**
     * 保存的AccessToken信息
     */
    List<AccessTokenInfo> listAccessToken(String userAccount, Date beginTime, Date endTime);
}
