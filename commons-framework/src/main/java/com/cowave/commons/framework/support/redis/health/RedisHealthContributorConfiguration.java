/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.health;

import java.util.Map;

import com.cowave.commons.framework.support.redis.StringRedisHelper;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnEnabledHealthIndicator("redis")
@Configuration
public class RedisHealthContributorConfiguration
        extends CompositeHealthContributorConfiguration<RedisHealthIndicator, StringRedisHelper> {

    @Bean
    public HealthContributor redisHealthIndicator(Map<String, StringRedisHelper> redisMap) {
        return createContributor(redisMap);
    }
}
