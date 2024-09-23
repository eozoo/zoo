/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import lombok.Data;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@ConditionalOnClass(MinioClient.class)
@ConfigurationProperties(prefix = "spring.minio")
@Configuration
public class MinioConfiguration {

    /**
     * Minio地址：127.0.0.1:9000
     */
    private String endpoint;

    /**
     * 访问key
     */
    private String accessKey;

    /**
     * 访问秘钥
     */
    private String secretKey;

    @ConditionalOnMissingBean(MinioClient.class)
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }

    @Bean
    public MinioService minioService(MinioClient minioClient) {
        return new MinioService(minioClient);
    }
}
