/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.access;

import com.cowave.commons.framework.helper.MessageHelper;
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
@ConfigurationProperties("spring.application.filter-access")
public class AccessFilterConfiguration {

    private String[] patterns = {"/*"};

    @Value("${info.cluster.id:}${server.port:8080}")
    private String idPrefix;

    private final MessageHelper messageHelper;

    @Nullable
    private final TransactionIdSetter transactionIdSetter;

    @Bean
    public FilterRegistrationBean<AccessFilter> accessFilterRegistration(){
        FilterRegistrationBean<AccessFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AccessFilter(messageHelper, transactionIdSetter, idPrefix));
        registration.setName("accessFilter");
        registration.addUrlPatterns(patterns);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
