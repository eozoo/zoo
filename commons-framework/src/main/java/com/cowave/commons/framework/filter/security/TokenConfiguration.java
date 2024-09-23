/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.security;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("spring.application.token")
public class TokenConfiguration {

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
