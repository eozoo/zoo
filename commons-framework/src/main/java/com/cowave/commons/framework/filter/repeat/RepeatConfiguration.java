/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.repeat;

import com.cowave.commons.framework.helper.MessageHelper;
import com.cowave.commons.framework.support.redis.RedisAutoConfiguration;
import com.cowave.commons.framework.support.redis.RedisHelper;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisOperations;

/**
 *
 * @author shanhuiming
 *
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@ConditionalOnClass(RedisOperations.class)
public class RepeatConfiguration {

    @ConditionalOnBean(RedisHelper.class)
    @Bean
    public RepeatInterceptor sameUrlDataInterceptorAbstract(RedisHelper redisHelper, MessageHelper messageHelper){
        return new RepeatInterceptor(redisHelper, messageHelper);
    }

    @ConditionalOnBean(RepeatInterceptor.class)
    @Bean
    public RepeatMvcConfigurer webMvcLimitConfigurer(RepeatInterceptor repeatInterceptor){
        return new RepeatMvcConfigurer(repeatInterceptor);
    }
}
