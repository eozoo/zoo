/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.feign.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.feign.annotation.FeignClient;

import com.cowave.commons.framework.filter.security.TokenService;
import com.cowave.commons.framework.configuration.ApplicationConfiguration;
import com.cowave.commons.framework.configuration.ClusterInfo;

import feign.RequestInterceptor;

import javax.annotation.Nullable;

/**
*
* @author shanhuiming
*
*/
@ConditionalOnClass(FeignClient.class)
@ConditionalOnMissingClass("io.seata.core.context.RootContext")
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class RequestIdInterceptorConfiguration {

    @Nullable
    private final TokenService tokenService;

    @ConditionalOnMissingBean(RequestInterceptor.class)
    @Bean
    public RequestInterceptor requestInterceptor(
            ApplicationConfiguration applicationConfiguration, ClusterInfo clusterInfo) {
        return new RequestIdInterceptor(applicationConfiguration, tokenService, clusterInfo);
    }
}
