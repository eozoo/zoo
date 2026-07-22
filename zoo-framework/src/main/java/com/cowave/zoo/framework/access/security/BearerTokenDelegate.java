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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.SignatureAlgorithm;

import java.security.Key;

/**
 * @author shanhuiming
 */
public interface BearerTokenDelegate {

    /**
     * 令牌类型
     */
    String CLAIM_TYPE = "tt";

    /**
     * 授权应用id
     */
    String CLAIM_OAUTH_ID = "oi";

    /**
     * 授权应用名称
     */
    String CLAIM_OAUTH_NAME = "on";

    /**
     * 访问IP
     */
    String CLAIM_ACCESS_IP = "ti";

    /**
     * Access令牌
     */
    String CLAIM_ACCESS_ID = "ta";

    /**
     * Refresh令牌
     */
    String CLAIM_REFRESH_ID = "tr";

    /**
     * 限制同一账号的登录设备
     */
    String CLAIM_ACCESS_UNIQUE = "tu";

    /**
     * 存储验证AccessToken
     */
    String CLAIM_ACCESS_VALID = "ts";

    /**
     * 租户id
     */
    String CLAIM_TENANT_ID = "ei";

    /**
     * 用户id
     */
    String CLAIM_USER_ID = "ui";

    /**
     * 用户编号
     */
    String CLAIM_USER_CODE = "uc";

    /**
     * 用户属性
     */
    String CLAIM_USER_PROPERTIES = "up";

    /**
     * 用户类型
     */
    String CLAIM_USER_TYPE = "ut";

    /**
     * 用户名称
     */
    String CLAIM_USER_NAME = "un";

    /**
     * 用户账号
     */
    String CLAIM_USER_ACCOUNT = "ua";

    /**
     * 用户角色
     */
    String CLAIM_USER_ROLE = "ur";

    /**
     * 用户权限
     */
    String CLAIM_USER_PERM = "um";

    /**
     * 部门id
     */
    String CLAIM_DEPT_ID = "di";

    /**
     * 部门编号
     */
    String CLAIM_DEPT_CODE = "dc";

    /**
     * 部门名称
     */
    String CLAIM_DEPT_NAME = "dn";

    /**
     * 集群id
     */
    String CLAIM_CLUSTER_ID = "ci";

    /**
     * 集群级别
     */
    String CLAIM_CLUSTER_LEVEL = "cl";

    /**
     * 集群名称
     */
    String CLAIM_CLUSTER_NAME = "cn";

    /**
     * Token保存方式（header、cookie）
     */
    String tokenStore();

    /**
     * Token保存的key
     */
    String tokenKey();

    /**
     * 认证方式：（basic、access、access-refresh）
     */
    AuthMode authMode();

    /**
     * 应用id，验证OAuth令牌是否有资格访问
     */
    String oauthAppId();

    /**
     * Http Response是否始终返回200状态码。
     */
    boolean alwaysReturnHttp200();

    /**
     * AccessToken Algorithm
     */
    SignatureAlgorithm getAccessAlgorithm();

    /**
     * AccessToken SigningKey
     */
    Key getAccessSigningKey(SignatureAlgorithm algorithm);

    /**
     * AccessToken VerificationKey
     */
    Key getAccessVerificationKey(SignatureAlgorithm algorithm);

    /**
     * AccessToken Issuer
     */
    String getAccessIssuer();

    /**
     * AccessToken Expire
     */
    Integer getAccessExpireSeconds();

    /**
     * AccessToken Claims
     */
    void setAccessClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails);

    /**
     * OAuth AccessToken Claims
     */
    void setOauthAccessClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails);

    /**
     * AccessToken Claims
     */
    AccessUserDetails parseAccessClaims(Claims claims);

    /**
     * RefreshToken Algorithm
     */
    SignatureAlgorithm getRefreshAlgorithm();

    /**
     * RefreshToken SigningKey
     */
    Key getRefreshSigningKey(SignatureAlgorithm algorithm);

    /**
     * RefreshToken VerificationKey
     */
    Key getRefreshVerificationKey(SignatureAlgorithm algorithm);

    /**
     * RefreshToken Issuer
     */
    String getRefreshIssuer();

    /**
     * RefreshToken Expire
     */
    Integer getRefreshExpireSeconds();

    /**
     * RefreshToken Claims
     */
    void setRefreshClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails);

    /**
     * OAuth RefreshToken Claims
     */
    void setOauthRefreshClaims(JwtBuilder jwtBuilder, AccessUserDetails userDetails);

    /**
     * RefreshToken Claims
     */
    AccessUserDetails parseRefreshClaims(Claims claims);

    /**
     * OAuth RefreshToken Claims
     */
    AccessUserDetails parseOauthRefreshClaims(Claims claims);
}
