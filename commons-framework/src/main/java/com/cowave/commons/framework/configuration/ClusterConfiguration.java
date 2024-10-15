/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
