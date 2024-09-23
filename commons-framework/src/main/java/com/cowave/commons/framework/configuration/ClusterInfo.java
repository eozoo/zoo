package com.cowave.commons.framework.configuration;

import java.util.Map;

import lombok.Data;

/**
 * @author shanhuiming
 */
@Data
public class ClusterInfo {

    private Integer id;

    private String name;

    private Integer level;

    private Map<String, Object> properties;
}
