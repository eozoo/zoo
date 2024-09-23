package com.cowave.commons.framework.support.redis.health;

import java.util.Map;

import com.cowave.commons.framework.support.redis.RedisHelper;
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
        extends CompositeHealthContributorConfiguration<RedisHealthIndicator, RedisHelper> {

    @Bean
    public HealthContributor redisHealthIndicator(Map<String, RedisHelper> redisMap) {
        return createContributor(redisMap);
    }
}
