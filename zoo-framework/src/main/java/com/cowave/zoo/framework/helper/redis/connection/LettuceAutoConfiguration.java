/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.helper.redis.connection;

import com.cowave.zoo.framework.helper.redis.RedisAutoConfiguration;
import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.NettyCustomizer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.util.concurrent.TimeUnit;

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

    @Value("${spring.redis.lettuce.idle.read:30}")
    private int readerIdleTime;

    @Value("${spring.redis.lettuce-idle.write:30}")
    private int writerIdleTime;

    @Value("${spring.redis.lettuce.idle.all:60}")
    private int allIdleTime;

    @Value("${spring.redis.lettuce.idle.check:false}")
    private boolean checkIdle;

    @ConditionalOnMissingBean
    @Primary
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources clientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        if (checkIdle) {
            NettyCustomizer nettyCustomizer = new NettyCustomizer() {
                @Override
                public void afterChannelInitialized(Channel channel) {
                    // 检测空闲状态
                    channel.pipeline().addLast(new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS));
                    // 断开处理空闲的连接，触发ConnectionWatchdog
                    channel.pipeline().addLast(new ChannelDuplexHandler() {
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                            if (evt instanceof IdleStateEvent) {
                                ctx.disconnect();
                            }
                        }
                    });
                }
            };
            builder.nettyCustomizer(nettyCustomizer);
        }
        return builder.build();
    }

    @ConditionalOnMissingBean(AbstractRedisConnectionConfiguration.class)
    @Primary
    @Bean
    public LettuceRedisConnectionConfiguration redisConnectionConfiguration(RedisProperties redisProperties,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        return new LettuceRedisConnectionConfiguration(redisProperties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    @Primary
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            ClientResources clientResources,
            LettuceRedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers) {
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientResources);
    }

    @Conditional(ZooRedisCondition.class)
    @Bean(destroyMethod = "shutdown")
    public DefaultClientResources zooClientResources(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach(customizer -> customizer.customize(builder));
        if (checkIdle) {
            NettyCustomizer nettyCustomizer = new NettyCustomizer() {
                @Override
                public void afterChannelInitialized(Channel channel) {
                    // 检测空闲状态
                    channel.pipeline().addLast(new IdleStateHandler(readerIdleTime, writerIdleTime, allIdleTime, TimeUnit.SECONDS));
                    // 断开处理空闲的连接，触发ConnectionWatchdog
                    channel.pipeline().addLast(new ChannelDuplexHandler() {
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
                            if (evt instanceof IdleStateEvent) {
                                ctx.disconnect();
                            }
                        }
                    });
                }
            };
            builder.nettyCustomizer(nettyCustomizer);
        }
        return builder.build();
    }

    @ConditionalOnMissingBean(name = "zooRedisConnectionConfiguration")
    @ConditionalOnBean(name = "zooClientResources")
    @Bean
    public LettuceRedisConnectionConfiguration zooRedisConnectionConfiguration(Environment environment,
            ObjectProvider<RedisSentinelConfiguration> sentinelConfigurationProvider,
            ObjectProvider<RedisClusterConfiguration> clusterConfigurationProvider) {
        RedisProperties properties = Binder.get(environment).bind("zoo.redis", RedisProperties.class).get();
        return new LettuceRedisConnectionConfiguration(properties, sentinelConfigurationProvider, clusterConfigurationProvider);
    }

    @ConditionalOnMissingBean(name = "zooRedisConnectionFactory")
    @ConditionalOnBean(name = "zooRedisConnectionConfiguration")
    @Bean
    public LettuceConnectionFactory zooRedisConnectionFactory(
            @Qualifier("zooClientResources") ClientResources clientResources,
            @Qualifier("zooRedisConnectionConfiguration") LettuceRedisConnectionConfiguration redisConnectionConfiguration,
            ObjectProvider<LettuceClientConfigurationBuilderCustomizer> builderCustomizers) {
        return redisConnectionConfiguration.redisConnectionFactory(builderCustomizers, clientResources);
    }
}
