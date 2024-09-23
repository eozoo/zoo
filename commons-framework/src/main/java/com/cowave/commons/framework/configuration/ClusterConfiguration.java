/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.Map;

/**
 * 
 * @author shanhuiming
 * 
 */
@Data
@Configuration(proxyBeanMethods = false)
@ConfigurationProperties("info.cluster")
public class ClusterConfiguration {

    /**
     * 集群id
     */
    private Integer id = 10;

    /**
     * 集群名称
     */
    private String name = "default";
    
    /**
     * 集群Level
     */
    private Integer level = 1;

    /**
     * 集群属性
     */
    private Map<String, Object> properties;

    @Bean
    public ClusterInfo nodeInfo() {
        ClusterInfo cluster = new ClusterInfo();
        cluster.setId(id);
        cluster.setName(name);
        cluster.setLevel(level);
        cluster.setProperties(properties);
        return cluster;
    }
}
