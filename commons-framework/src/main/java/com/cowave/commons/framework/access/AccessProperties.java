/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access;

import com.cowave.commons.framework.access.security.BasicAuthUser;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@ConfigurationProperties("spring.access")
public class AccessProperties {

    /**
     * AccessResponse是否永远200
     */
    private boolean alwaysSuccess;

    /**
     * AccessFilter拦截的url
     */
    private String[] patterns = {"/*"};

    /**
     * CSP白名单
     * <p/> script-src 'self'
     * <p/> default-src * data: blob: 'unsafe-inline' 'unsafe-eval'
     */
    private String contentSecurityPolicy;

    /**
     * 跨域设置
     */
    private CrossControl control = new CrossControl();

    /**
     * Access鉴权配置
     */
    private AuthConfig auth;

    public boolean authEnable(){
        if(auth != null){
            return auth.enable;
        }
        return true;
    }

    public List<BasicAuthUser> basicUsers(){
        if(auth != null){
            return auth.basicUsers;
        }
        return List.of(new BasicAuthUser("cowave", "Cowave@123", new String[]{"sysAdmin"}));
    }

    public String[] basicAuthUrls(){
        if(auth != null){
            return auth.basicAuthUrls;
        }
        return new String[]{"/actuator/**"};
    }

    public String[] basicIgnoreUrls(){
        if(auth == null){
            return new String[0];
        }
        if(auth.basicIgnoreUrls == null){
            return new String[0];
        }
        return auth.basicIgnoreUrls;
    }

    public String tokenStore(){
        if(auth != null){
            return auth.tokenStore;
        }
        return "header";
    }

    public String tokenKey(){
        if(auth != null){
            return auth.tokenKey;
        }
        return "Authorization";
    }

    public boolean conflict(){
        if(auth != null){
            return auth.conflict;
        }
        return true;
    }

    public int accessExpire(){
        if(auth != null){
            return auth.accessExpire;
        }
        return 3600;
    }

    public String accessSecret(){
        if(auth != null){
            return auth.accessSecret;
        }
        return "access@cowave.com";
    }

    public int refreshExpire(){
        if(auth != null){
            return auth.refreshExpire;
        }
        return 3600 * 24 * 7;
    }

    public String refreshSecret(){
        if(auth != null){
            return auth.refreshSecret;
        }
        return "refresh@cowave.com";
    }

    public String[] tokenAuthUrls(){
        if(auth == null){
            return new String[0];
        }
        if(auth.tokenAuthUrls == null){
            return new String[0];
        }
        return auth.tokenAuthUrls;
    }

    public String[] tokenIgnoreUrls(){
        if(auth == null){
            return new String[0];
        }
        if(auth.tokenIgnoreUrls == null){
            return new String[0];
        }
        return auth.tokenIgnoreUrls;
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
         * 是否开启认证
         */
        private boolean enable = true;

        /**
         * 认证方式：（basic、accessToken、refreshToken）
         */
        private String mode = "basic";

        /**
         * basic认证的url
         */
        private String[] basicAuthUrls;

        /**
         * basic忽略的url
         */
        private String[] basicIgnoreUrls;

        /**
         * basic默认用户
         */
        private List<BasicAuthUser> basicUsers = List.of(new BasicAuthUser("cowave", "Cowave@123", new String[]{"sysAdmin"}));

        /**
         * Token保存方式（header、cookie）
         */
        private String tokenStore = "header";

        /**
         * Token保存的key
         */
        private String tokenKey = "Authorization";

        /**
         * 是否检查冲突
         */
        private boolean conflict = true;

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

        /**
         * token认证的url
         */
        private String[] tokenAuthUrls;

        /**
         * token忽略的url
         */
        private String[] tokenIgnoreUrls;
    }
}
