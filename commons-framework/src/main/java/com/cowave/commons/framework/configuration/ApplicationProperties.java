package com.cowave.commons.framework.configuration;

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
