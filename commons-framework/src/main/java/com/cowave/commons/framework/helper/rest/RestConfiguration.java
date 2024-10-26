package com.cowave.commons.framework.helper.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author shanhuiming
 *
 */
@Configuration
public class RestConfiguration {

    @Bean
    @ConditionalOnMissingBean(RestTemplate.class)
    public RestTemplate restTemplate(RestTemplateBuilder builder, ClientHttpRequestInterceptor requestInterceptor) {
        return builder.additionalInterceptors(requestInterceptor).build();
    }
}
