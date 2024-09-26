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
import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 *
 * @author shanhuiming
 *
 */
@AutoConfigureBefore({RedisAutoConfiguration.class})
@ConditionalOnProperty(name = "spring.redis.client-type", havingValue = "lettuce", matchIfMissing = true)
@ConditionalOnClass(RedisClient.class)
@EnableConfigurationProperties({RedisProperties.class})
public class LettuceAutoConfiguration {

    @Primary
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources clientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    @Primary
    @Bean
    public LettuceRedisConnectionConfiguration redisConnectionConfiguration(RedisProperties redisProperties,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        return new LettuceRedisConnectionConfiguration(redisProperties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @Primary
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            ClientResources clientResources,
            LettuceRedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers) {
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientResources);
    }

    @Conditional(CommonRedisCondition.class)
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources commonClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        return builder.build();
    }

    @ConditionalOnBean(name = "commonClientResources")
    @Bean
    public LettuceRedisConnectionConfiguration commonRedisConnectionConfiguration(Environment environment,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        RedisProperties properties = Binder.get(environment).bind("common.redis", RedisProperties.class).get();
        return new LettuceRedisConnectionConfiguration(properties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @ConditionalOnBean(name = "commonRedisConnectionConfiguration")
    @Bean
    public LettuceConnectionFactory commonRedisConnectionFactory(
            @Qualifier("commonClientResources") ClientResources clientResources,
            @Qualifier("commonRedisConnectionConfiguration") LettuceRedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers) {
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientResources);
    }
}
