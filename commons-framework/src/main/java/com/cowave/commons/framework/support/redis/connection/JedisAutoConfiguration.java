/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.connection;

import com.cowave.commons.framework.support.redis.RedisAutoConfiguration;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

/**
 *
 * @author shanhuiming
 *
 */
@AutoConfigureBefore({RedisAutoConfiguration.class})
@ConditionalOnProperty(name = "spring.redis.client-type", havingValue = "jedis", matchIfMissing = true)
@ConditionalOnClass({ GenericObjectPool.class, JedisConnection.class, Jedis.class })
@EnableConfigurationProperties({RedisProperties.class})
public class JedisAutoConfiguration {

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public JedisRedisConnectionConfiguration redisConnectionConfiguration(RedisProperties redisProperties,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
            ObjectProvider<RedisClusterConfiguration> clusterConfiguration){
        return new JedisRedisConnectionConfiguration(redisProperties, sentinelConfiguration, clusterConfiguration);
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public JedisConnectionFactory redisConnectionFactory(
            JedisRedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }

    @Conditional(CommonRedisCondition.class)
    @Bean
    public JedisRedisConnectionConfiguration commonRedisConnectionConfiguration(Environment environment,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
            ObjectProvider<RedisClusterConfiguration> clusterConfiguration){
        RedisProperties properties = Binder.get(environment).bind("common.redis", RedisProperties.class).get();
        return new JedisRedisConnectionConfiguration(properties, sentinelConfiguration, clusterConfiguration);
    }

    @ConditionalOnBean(name = "commonRedisConnectionFactory")
    @Bean
    public JedisConnectionFactory commonRedisConnectionFactory(
            @Qualifier("commonRedisConnectionConfiguration") JedisRedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }
}
