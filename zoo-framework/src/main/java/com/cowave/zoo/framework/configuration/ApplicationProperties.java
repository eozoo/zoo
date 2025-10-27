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
package com.cowave.zoo.framework.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@ConfigurationProperties(prefix = "spring.application")
public class ApplicationProperties {

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 集群id
     */
    private int clusterId = 10;

    /**
     * 集群等级
     */
    private int clusterLevel = 1;

    /**
     * 集群名称
     */
    private String clusterName = "default";

    /**
     * 集群属性
     */
    private Map<String, Object> clusterProperties;
}
