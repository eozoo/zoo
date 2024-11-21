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
    private TokenConfig token;

    /**
     * security默认用户
     */
    private List<BasicAuthUser> securityUsers = List.of(new BasicAuthUser("cowave", "Cowave@123", new String[]{"sysAdmin"}));

    /**
     * security默认需要认证的url
     */
    private String[] securityUrls = new String[]{"/actuator/**"};

    public String tokenStore(){
        if(token != null){
            return token.store;
        }
        return "header";
    }

    public String tokenStoreKey(){
        if(token != null){
            return token.storeKey;
        }
        return "Authorization";
    }

    public String tokenMode(){
        if(token != null){
            return token.mode;
        }
        return "basic";
    }

    public boolean tokenConflict(){
        if(token != null){
            return token.conflict;
        }
        return true;
    }

    public int tokenAccessExpire(){
        if(token != null){
            return token.accessExpire;
        }
        return 3600;
    }

    public String tokenAccessSecret(){
        if(token != null){
            return token.accessSecret;
        }
        return "access@cowave.com";
    }

    public int tokenRefreshExpire(){
        if(token != null){
            return token.refreshExpire;
        }
        return 3600 * 24 * 7;
    }

    public String tokenRefreshSecret(){
        if(token != null){
            return token.refreshSecret;
        }
        return "refresh@cowave.com";
    }

    public String[] tokenIgnoreUrls(){
        if(token == null){
            return new String[0];
        }
        if(token.ignoreUrls == null){
            return new String[0];
        }
        return token.ignoreUrls;
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
    public static class TokenConfig {

        /**
         * Token保存方式（header、cookie）
         */
        private String store = "header";

        /**
         * Token保存的key
         */
        private String storeKey = "Authorization";

        /**
         * 安全模式（basic、access、access-refresh）
         */
        private String mode = "basic";

        /**
         * 是否检查冲突
         */
        private boolean conflict = true;

        /**
         * accessToken超时
         */
        private int accessExpire = 3600;

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
         * 忽略鉴权的url
         */
        private String[] ignoreUrls;
    }
}
