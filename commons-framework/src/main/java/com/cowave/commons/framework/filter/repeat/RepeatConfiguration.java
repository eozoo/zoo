/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.filter.repeat;

import com.cowave.commons.framework.configuration.AccessConfiguration;
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
    public RepeatInterceptor sameUrlDataInterceptorAbstract(RedisHelper redisHelper, AccessConfiguration accessConfiguration){
        return new RepeatInterceptor(redisHelper, accessConfiguration.isAlwaysSuccess());
    }

    @ConditionalOnBean(RepeatInterceptor.class)
    @Bean
    public RepeatMvcConfigurer webMvcLimitConfigurer(RepeatInterceptor repeatInterceptor){
        return new RepeatMvcConfigurer(repeatInterceptor);
    }
}
