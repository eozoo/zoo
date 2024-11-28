/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.rest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@ConfigurationProperties("spring.rest")
public class RestProperties {

    /**
     * 连接超时
     */
    private int connectTimeout = 10 * 1000;

    /**
     * 响应超时
     */
    private int socketTimeout = 120 * 1000;

    /**
     * 获取连接超时
     */
    private int poolTimeout = 30 * 1000;

    /**
     * 最大连接数
     */
    private int maxConnections = 100;

    /**
     * 最大重试次数
     */
    private int maxRetry = 3;

    /**
     * 重试间隔
     */
    private long retryInterval = 1000;
}
