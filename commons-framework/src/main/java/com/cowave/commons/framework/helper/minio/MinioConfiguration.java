package com.cowave.commons.framework.helper.minio;

import io.minio.MinioClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(MinioClient.class)
@Configuration
@EnableConfigurationProperties({MinioProperties.class})
public class MinioConfiguration {

    @ConditionalOnMissingBean(MinioClient.class)
    @Bean
    public MinioClient minioClient(MinioProperties minioProperties) {
        return MinioClient.builder().endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey()).build();
    }

    @Bean
    public MinioHelper minioFileHelper(MinioClient minioClient) {
        return new MinioHelper(minioClient);
    }
}
