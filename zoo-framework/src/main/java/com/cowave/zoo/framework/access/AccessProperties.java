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
package com.cowave.zoo.framework.access;

import com.cowave.zoo.framework.access.security.AccessUser;
import com.cowave.zoo.framework.access.security.AuthMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.cowave.zoo.framework.access.security.AuthMode.BASIC;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@RefreshScope
@Component
@ConfigurationProperties("spring.access")
public class AccessProperties {

    /**
     * AccessResponse是否永远200
     */
    private boolean alwaysSuccess;

    /**
     * AccessFilter拦截的url
     */
    private String[] filter = {"/*"};

    /**
     * CSP白名单
     * <p/> script-src 'self'
     * <p/> default-src * data: blob: 'unsafe-inline' 'unsafe-eval'
     */
    private String contentSecurityPolicy;

    /**
     * 跨域设置
     */
    private CrossControl cross = new CrossControl();

    /**
     * Access鉴权配置
     */
    private AuthConfig auth;

    /**
     * OAuth授权配置
     */
    private OauthConfig oauth;

    public boolean authEnable(){
        return Optional.ofNullable(auth).map(auth -> auth.enable).orElse(false);
    }

    public AuthMode authMode(){
        return Optional.ofNullable(auth).map(auth -> auth.mode).orElse(BASIC);
    }

    public String[] authUrls(){
        return Optional.ofNullable(auth).map(auth -> auth.authUrls).orElse(null);
    }

    public String[] ignoreUrls(){
        return Optional.ofNullable(auth).map(auth -> auth.ignoreUrls).orElse(null);
    }

    public String[] basicUrls(){
        return Optional.ofNullable(auth).map(auth -> auth.basicUrls).orElse(null);
    }

    public boolean basicWithConfigUser(){
         return Optional.ofNullable(auth).map(auth -> auth.basicWithConfigUser).orElse(false);
    }

    public List<AccessUser> accessUsers(){
        return Optional.ofNullable(auth).map(auth -> auth.users).orElse(List.of(AccessUser.defaultUser()));
    }

    public String tokenStore(){
        return Optional.ofNullable(auth).map(auth -> auth.tokenStore).orElse("header");
    }

    public String tokenKey(){
        return Optional.ofNullable(auth).map(auth -> auth.tokenKey).orElse("Authorization");
    }

    public int accessExpire(){
        return Optional.ofNullable(auth).map(auth -> auth.accessExpire).orElse(86400);
    }

    public String accessSecret(){
        return Optional.ofNullable(auth).map(auth -> auth.accessSecret).orElse("access@cowave.com");
    }

    public int refreshExpire(){
        return Optional.ofNullable(auth).map(auth -> auth.refreshExpire).orElse(86400 * 7);
    }

    public String refreshSecret(){
        return Optional.ofNullable(auth).map(auth -> auth.refreshSecret).orElse("refresh@cowave.com");
    }

    public String oauthAppId(){
        return Optional.ofNullable(oauth).map(oauth -> oauth.appId).orElse(null);
    }

    public String oauthAppSecret(){
        return Optional.ofNullable(oauth).map(oauth -> oauth.appSecret).orElse(null);
    }

    public String oauthAppRedirectUri(){
        return Optional.ofNullable(oauth).map(oauth -> oauth.appRedirectUri).orElse(null);
    }

    public String oauthCodeUri(){
        return Optional.ofNullable(oauth).map(oauth -> oauth.oauthCodeUri).orElse(null);
    }

    public String oauthTokenUri(){
        return Optional.ofNullable(oauth).map(oauth -> oauth.oauthTokenUri).orElse(null);
    }

    @Data
    public static class CrossControl {

        /**
         * 跨域调用允许的域名
         */
        private String allowOrigin = "*";

        /**
         * 跨域调用允许的方法
         */
        private String allowMethods = "*";

        /**
         * 跨域调用允许的Header
         */
        private String allowHeaders = "*";

        /**
         * 跨域调用允许包含用户凭据
         */
        private boolean allowCredentials = true;
    }

    @Data
    public static class AuthConfig {

        /**
         * 是否开启访问鉴权
         */
        private boolean enable;

        /**
         * 默认用户
         */
        private List<AccessUser> users = List.of(AccessUser.defaultUser());

        /**
         * 认证方式：（basic、access、access-refresh）
         */
        private AuthMode mode = BASIC;

        /**
         * 认证的url
         */
        private String[] authUrls;

        /**
         * 忽略的url
         */
        private String[] ignoreUrls;

        /**
         * Token模式中使用basic认证的url
         */
        private String[] basicUrls;

        /**
         * basic认证使用配置用户
         */
        private boolean basicWithConfigUser = false;

        /**
         * Token保存方式（header、cookie）
         */
        private String tokenStore = "header";

        /**
         * Token保存的key
         */
        private String tokenKey = "Authorization";

        /**
         * accessToken超时
         */
        private int accessExpire = 86400;

        /**
         * accessToken密钥
         */
        private String accessSecret = "access@cowave.com";

        /**
         * refreshToken超时
         */
        private int refreshExpire = 3600 * 24 * 7;

        /**
         * refreshToken密钥
         */
        private String refreshSecret = "refresh@cowave.com";
    }

    @Data
    public static class OauthConfig {

        /**
         * 应用id
         */
        private String appId;

        /**
         * 应用密钥
         */
        private String appSecret;

        /**
         * 重定向地址
         */
        private String appRedirectUri;

        /**
         * 授权码地址
         */
        private String oauthCodeUri;

        /**
         * 令牌地址
         */
        private String oauthTokenUri;
    }
}
