/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.redisson;

import com.cowave.commons.framework.support.redis.connection.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.util.ReflectionUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Configuration
@ConditionalOnClass({Redisson.class, RedisOperations.class})
@AutoConfigureBefore({RedissonAutoConfiguration.class, LettuceAutoConfiguration.class, JedisAutoConfiguration.class})
public class MultiRedissonAutoConfiguration {

    private final ApplicationContext ctx;

    @Nullable
    private final List<RedissonAutoConfigurationCustomizer> redissonAutoConfigurationCustomizers;

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Bean(destroyMethod = "shutdown")
    public RedissonClient privateRedisson(Environment environment) throws IOException {
        RedisProperties redisProperties = Binder.get(environment).bind("spring.redis.private", RedisProperties.class).get();
        RedissonProperties redissonProperties = Binder.get(environment).bind("spring.redis.private.redisson", RedissonProperties.class).orElse(null);
        return redisson(redisProperties, redissonProperties);
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Lazy
    @Bean
    public RedissonReactiveClient privateRedissonReactive(RedissonClient redisson) {
        return redisson.reactive();
    }

    @Conditional(MultiPrivateRedisCondition.class)
    @Primary
    @Lazy
    @Bean
    public RedissonRxClient privateRedissonRxJava(RedissonClient redisson) {
        return redisson.rxJava();
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Bean(destroyMethod = "shutdown")
    public RedissonClient publicRedisson(Environment environment) throws IOException {
        RedisProperties redisProperties = Binder.get(environment).bind("spring.redis.public", RedisProperties.class).get();
        RedissonProperties redissonProperties = Binder.get(environment).bind("spring.redis.public.redisson", RedissonProperties.class).orElse(null);
        return redisson(redisProperties, redissonProperties);
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Lazy
    @Bean
    public RedissonReactiveClient publicRedissonReactive(@Qualifier("publicRedisson") RedissonClient redisson) {
        return redisson.reactive();
    }

    @Conditional(MultiPublicRedisCondition.class)
    @Lazy
    @Bean
    public RedissonRxClient publicredissonRxJava(@Qualifier("publicRedisson") RedissonClient redisson) {
        return redisson.rxJava();
    }

    private RedissonClient redisson(RedisProperties redisProperties, RedissonProperties redissonProperties) throws IOException {
        Config config;
        Method clusterMethod = ReflectionUtils.findMethod(RedisProperties.class, "getCluster");
        Method timeoutMethod = ReflectionUtils.findMethod(RedisProperties.class, "getTimeout");
        Object timeoutValue = ReflectionUtils.invokeMethod(timeoutMethod, redisProperties);
        int timeout;
        Method nodesMethod;
        if (null == timeoutValue) {
            timeout = 10000;
        } else if (!(timeoutValue instanceof Integer)) {
            nodesMethod = ReflectionUtils.findMethod(timeoutValue.getClass(), "toMillis");
            timeout = ((Long)ReflectionUtils.invokeMethod(nodesMethod, timeoutValue)).intValue();
        } else {
            timeout = (Integer)timeoutValue;
        }

        if(redissonProperties != null){
            if (redissonProperties.getConfig() != null) {
                try {
                    config = Config.fromYAML(redissonProperties.getConfig());
                } catch (IOException var13) {
                    try {
                        config = Config.fromJSON(redissonProperties.getConfig());
                    } catch (IOException var12) {
                        throw new IllegalArgumentException("Can't parse config", var12);
                    }
                }
            } else if (redissonProperties.getFile() != null) {
                try {
                    InputStream is = this.getConfigStream(redissonProperties);
                    config = Config.fromYAML(is);
                } catch (IOException var11) {
                    try {
                        InputStream is = this.getConfigStream(redissonProperties);
                        config = Config.fromJSON(is);
                    } catch (IOException var10) {
                        throw new IllegalArgumentException("Can't parse config", var10);
                    }
                }
            }else{
                config = new Config();
            }
        } else if (redisProperties.getSentinel() != null) {
            nodesMethod = ReflectionUtils.findMethod(RedisProperties.Sentinel.class, "getNodes");
            Object nodesValue = ReflectionUtils.invokeMethod(nodesMethod, redisProperties.getSentinel());
            String[] nodes;
            if (nodesValue instanceof String) {
                nodes = this.convert(Arrays.asList(((String)nodesValue).split(",")));
            } else {
                nodes = this.convert((List)nodesValue);
            }
            config = new Config();
            SentinelServersConfig sentinelServersConfig = config.useSentinelServers()
                    .setMasterName(redisProperties.getSentinel().getMaster())
                    .addSentinelAddress(nodes)
                    .setDatabase(redisProperties.getDatabase())
                    .setConnectTimeout(timeout);
            if(StringUtils.isNotBlank(redisProperties.getPassword())){
                sentinelServersConfig.setPassword(redisProperties.getPassword());
            }
        } else {
            Method method;
            if (clusterMethod != null && ReflectionUtils.invokeMethod(clusterMethod, redisProperties) != null) {
                Object clusterObject = ReflectionUtils.invokeMethod(clusterMethod, redisProperties);
                method = ReflectionUtils.findMethod(clusterObject.getClass(), "getNodes");
                List<String> nodesObject = (List)ReflectionUtils.invokeMethod(method, clusterObject);
                String[] nodes = this.convert(nodesObject);
                config = new Config();
                config.useClusterServers().addNodeAddress(nodes).setConnectTimeout(timeout).setPassword(redisProperties.getPassword());
            } else {
                config = new Config();
                String prefix = "redis://";
                method = ReflectionUtils.findMethod(RedisProperties.class, "isSsl");
                if (method != null && (Boolean)ReflectionUtils.invokeMethod(method, redisProperties)) {
                    prefix = "rediss://";
                }
                config.useSingleServer().setAddress(prefix + redisProperties.getHost() + ":" + redisProperties.getPort()).setConnectTimeout(timeout).setDatabase(redisProperties.getDatabase()).setPassword(redisProperties.getPassword());
            }
        }

        if (this.redissonAutoConfigurationCustomizers != null) {
            for (RedissonAutoConfigurationCustomizer customizer : this.redissonAutoConfigurationCustomizers) {
                customizer.customize(config);
            }
        }
        return Redisson.create(config);
    }

    private String[] convert(List<String> nodesObject) {
        List<String> nodes = new ArrayList<>(nodesObject.size());
        Iterator<String> it = nodesObject.iterator();
        while(it.hasNext()) {
            String node = it.next();
            if (!node.startsWith("redis://") && !node.startsWith("rediss://")) {
                nodes.add("redis://" + node);
            } else {
                nodes.add(node);
            }
        }
        return nodes.toArray(new String[nodes.size()]);
    }

    private InputStream getConfigStream(RedissonProperties redissonProperties) throws IOException {
        Resource resource = this.ctx.getResource(redissonProperties.getFile());
        return resource.getInputStream();
    }
}
