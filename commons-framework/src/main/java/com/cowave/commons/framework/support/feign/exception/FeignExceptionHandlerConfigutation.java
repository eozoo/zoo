/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.feign.exception;

import io.seata.core.context.RootContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.feign.FeignExceptionHandler;
import org.springframework.feign.annotation.FeignClient;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({FeignClient.class, RootContext.class})
@Configuration
public class FeignExceptionHandlerConfigutation {

    @ConditionalOnMissingBean(FeignExceptionHandler.class)
    @Bean
    public FeignRollbackHandler feignRollbackHandler(){
        return new FeignRollbackHandler();
    }
}
