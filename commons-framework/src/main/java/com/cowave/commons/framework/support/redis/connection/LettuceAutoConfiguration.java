package com.cowave.commons.framework.support.redis.connection;

import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
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
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(RedisClient.class)
@ConditionalOnProperty(name = "spring.redis.client-type", havingValue = "lettuce", matchIfMissing = true)
@EnableConfigurationProperties({RedisProperties.class})
public class LettuceAutoConfiguration {

    private final RedisProperties redisProperties;

    public LettuceAutoConfiguration(RedisProperties redisProperties){
        this.redisProperties = redisProperties;
    }

    @ConditionalOnMissingBean(DefaultClientResources.class)
    @Conditional(MultiOriginRedisCondition.class)
    @Primary
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources clientResources() {
        return DefaultClientResources.create();
    }

    @ConditionalOnMissingBean(RedisLettuceConnectionConfiguration.class)
    @Conditional(MultiOriginRedisCondition.class)
    @Primary
    @Bean
    public RedisLettuceConnectionConfiguration redisConnectionConfiguration(
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider){
        return new RedisLettuceConnectionConfiguration(redisProperties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @ConditionalOnMissingBean(LettuceConnectionFactory.class)
    @Conditional(MultiOriginRedisCondition.class)
    @Primary
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            ClientResources clientResources,
            RedisLettuceConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientResources);
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources privateClientResources() {
        return DefaultClientResources.create();
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Bean
    public RedisLettuceConnectionConfiguration privateRedisConnectionConfiguration(
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
            Environment environment){
        RedisProperties properties = Binder.get(environment).bind("spring.redis.private", RedisProperties.class).get();
        return new RedisLettuceConnectionConfiguration(properties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Bean
    public LettuceConnectionFactory privateRedisConnectionFactory(
            ClientResources clientResources,
            RedisLettuceConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientResources);
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources publicClientResources() {
        return DefaultClientResources.create();
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Bean
    public RedisLettuceConnectionConfiguration publicRedisConnectionConfiguration(
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider,
            Environment environment){
        RedisProperties properties = Binder.get(environment).bind("spring.redis.public", RedisProperties.class).get();
        return new RedisLettuceConnectionConfiguration(properties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Bean
    public LettuceConnectionFactory publicRedisConnectionFactory(
            @Qualifier("publicClientResources") ClientResources clientResources,
            @Qualifier("publicRedisConnectionConfiguration") RedisLettuceConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientResources);
    }
}
