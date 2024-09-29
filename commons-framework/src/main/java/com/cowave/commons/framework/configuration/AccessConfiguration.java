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
     * Access操作日志
     */
    private OplogConfig oplog;

    /**
     * Access异常告警
     */
    private AlarmConfig alarm;

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
    public static class OplogConfig {

        private boolean kafkaEnable = true;

        private String kafkaTopic = "access-oplog";
    }

    @Data
    public static class AlarmConfig {

        private boolean kafkaEnable = true;

        private String kafkaTopic = "access-alarm";
    }
}
