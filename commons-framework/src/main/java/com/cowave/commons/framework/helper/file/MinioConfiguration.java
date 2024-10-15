/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
