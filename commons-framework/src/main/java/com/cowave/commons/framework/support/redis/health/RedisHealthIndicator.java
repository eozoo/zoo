/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.redis.health;

import java.util.*;

import com.cowave.commons.framework.support.redis.RedisHelper;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

/**
 *
 * @author shanhuiming
 *
 */
public class RedisHealthIndicator extends AbstractHealthIndicator {

    private final RedisHelper redis;

    public RedisHealthIndicator(RedisHelper redis) {
        this.redis = redis;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try{
            redis.ping();
            builder.up();

            Properties info = redis.info();
            assert info != null;
            RedisConnectionFactory connectionFactory = redis.getRedisTemplate().getConnectionFactory();

            assert connectionFactory != null;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("redis_version", info.get("redis_version"));
            if("standalone".equals(info.get("redis_mode"))){
                map.put("redis_mode", info.get("redis_mode"));
                map.put("redis_port", info.get("tcp_port"));
                if(connectionFactory instanceof LettuceConnectionFactory lettuce){
                    map.put("redis_host", lettuce.getHostName());
                }

            }else if("sentinel".equals(info.get("redis_mode"))){
                map.put("redis_mode", info.get("sentinel"));
                if(connectionFactory instanceof LettuceConnectionFactory lettuce){
                    RedisConfiguration.SentinelConfiguration sentinel = lettuce.getSentinelConfiguration();
                    if(sentinel != null){
                        NamedNode master = sentinel.getMaster();
                        if(master != null){
                            map.put("master", master.getName());
                        }

                        Set<RedisNode> nodes = sentinel.getSentinels();
                        StringBuilder build = new StringBuilder();
                        for(RedisNode node : nodes){
                            build.append(node.getHost()).append(":").append(node.getPort()).append(",");
                        }
                        map.put("nodes", build.subSequence(0, build.length() - 1));
                    }
                }

            }else if("cluster".equals(info.get("redis_mode"))) {
                map.put("redis_mode", info.get("cluster"));
                if(connectionFactory instanceof LettuceConnectionFactory lettuce){
                    RedisConfiguration.ClusterConfiguration cluster = lettuce.getClusterConfiguration();
                    if(cluster != null){
                        Set<RedisNode> nodes = cluster.getClusterNodes();
                        StringBuilder build = new StringBuilder();
                        for (RedisNode node : nodes) {
                            build.append(node.getHost()).append(":").append(node.getPort()).append(",");
                        }
                        map.put("nodes", build.subSequence(0, build.length() - 1));
                    }
                }
            }
            map.put("memory_used", info.get("used_memory_human"));
            map.put("connected_clients", info.get("connected_clients"));
            map.put("uptime_in_days", info.get("uptime_in_days"));
            builder.withDetails(map);
        }catch (Exception e){
            builder.down();
        }
    }
}
