/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("spring.access")
public class AccessConfiguration {

    /**
     * AccessResponse是否永远200
     */
    private boolean alwaysSuccess;

    /**
     * AccessFilter拦截的url
     */
    private String[] patterns = {"/*"};

    /**
     * Access鉴权配置
     */
    private TokenConfig token;

    /**
     * Access异常告警
     */
    private AlarmConfig alarm;

    public String tokenHeader(){
        if(token != null){
            return token.header;
        }
        return "Authorization";
    }

    public String tokenSalt(){
        if(token != null){
            return token.salt;
        }
        return "admin@cowave.com";
    }
    public boolean tokenConflict(){
        if(token != null){
            return token.conflict;
        }
        return false;
    }

    public int tokenAccessExpire(){
        if(token != null){
            return token.accessExpire;
        }
        return 3600;
    }

    public int tokenRefreshExpire(){
        if(token != null){
            return token.refreshExpire;
        }
        return 3600 * 24 * 7;
    }

    public List<String> tokenIgnoreUrls(){
        if(token != null){
            return token.ignoreUrls;
        }
        return new ArrayList<>();
    }

    public boolean alarmKafkaEnable(){
        if(alarm != null){
            return alarm.kafkaEnable;
        }
        return true;
    }

    public String alarmKafkaTopic(){
        if(alarm != null){
            return alarm.kafkaTopic;
        }
        return "access-alarm";
    }


    @Data
    public static class TokenConfig {

        /**
         * header名称
         */
        private String header = "Authorization";

        /**
         * 秘钥
         */
        private String salt = "admin@cowave.com";

        /**
         * 是否检查冲突
         */
        private boolean conflict = false;

        /**
         * accessToken超时
         */
        private int accessExpire = 3600;

        /**
         * refreshToken超时
         */
        private int refreshExpire = 3600 * 24 * 7;

        /**
         * 忽略鉴权的url
         */
        private List<String> ignoreUrls = new ArrayList<>();
    }

    @Data
    public static class AlarmConfig {

        private boolean kafkaEnable = true;

        private String kafkaTopic = "access-alarm";
    }
}
