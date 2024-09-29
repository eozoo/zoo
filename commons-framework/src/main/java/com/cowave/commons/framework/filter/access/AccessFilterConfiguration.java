/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.access;

import com.cowave.commons.framework.configuration.AccessConfiguration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@RequiredArgsConstructor
@Configuration
@ConfigurationProperties("spring.access.filter")
public class AccessFilterConfiguration {

    private String[] patterns = {"/*"};

    @Nullable
    private final TransactionIdSetter transactionIdSetter;

    @Bean
    public AccessIdGenerator accessIdGenerator(@Value("${info.cluster.id:}${server.port:8080}") String idPrefix){
        return new AccessIdGenerator(idPrefix);
    }

    @Bean
    public FilterRegistrationBean<AccessFilter> accessFilterRegistration(AccessIdGenerator accessIdGenerator, AccessConfiguration accessConfiguration){
        FilterRegistrationBean<AccessFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccessFilter(transactionIdSetter, accessIdGenerator, accessConfiguration.isAlwaysSuccess()));
        registration.setName("accessFilter");
        registration.addUrlPatterns(patterns);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
