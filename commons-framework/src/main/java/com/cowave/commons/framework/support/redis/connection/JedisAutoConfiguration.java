package com.cowave.commons.framework.support.redis.connection;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.JedisClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

/**
 *
 * @author shanhuiming
 *
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ GenericObjectPool.class, JedisConnection.class, Jedis.class })
@ConditionalOnMissingBean(RedisConnectionFactory.class)
@ConditionalOnProperty(name = "spring.redis.client-type", havingValue = "jedis", matchIfMissing = true)
@EnableConfigurationProperties({RedisProperties.class})
public class JedisAutoConfiguration {

    private final RedisProperties redisProperties;

    public JedisAutoConfiguration(RedisProperties redisProperties){
        this.redisProperties = redisProperties;
    }

    @Conditional(MultiOriginRedisCondition.class)
    @Primary
    @Bean
    public RedisJedisConnectionConfiguration redisConnectionConfiguration(
            ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
            ObjectProvider<RedisClusterConfiguration> clusterConfiguration){
        return new RedisJedisConnectionConfiguration(redisProperties, sentinelConfiguration, clusterConfiguration);
    }

    @Conditional(MultiOriginRedisCondition.class)
    @Primary
    @Bean
    public JedisConnectionFactory redisConnectionFactory(
            RedisJedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Bean
    public RedisJedisConnectionConfiguration privateRedisConnectionConfiguration(
            ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
            ObjectProvider<RedisClusterConfiguration> clusterConfiguration,
            Environment environment){
        RedisProperties properties = Binder.get(environment).bind("spring.redis.private", RedisProperties.class).get();
        return new RedisJedisConnectionConfiguration(properties, sentinelConfiguration, clusterConfiguration);
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Bean
    public JedisConnectionFactory privateRedisConnectionFactory(
            RedisJedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Bean
    public RedisJedisConnectionConfiguration publicRedisConnectionConfiguration(
            ObjectProvider<RedisSentinelConfiguration> sentinelConfiguration,
            ObjectProvider<RedisClusterConfiguration> clusterConfiguration,
            Environment environment){
        RedisProperties properties = Binder.get(environment).bind("spring.redis.public", RedisProperties.class).get();
        return new RedisJedisConnectionConfiguration(properties, sentinelConfiguration, clusterConfiguration);
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Bean
    public JedisConnectionFactory publicRedisConnectionFactory(
            @Qualifier("publicRedisConnectionConfiguration") RedisJedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<JedisClientConfigurationBuilderCustomizer> builderCustomizers){
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers);
    }
}
