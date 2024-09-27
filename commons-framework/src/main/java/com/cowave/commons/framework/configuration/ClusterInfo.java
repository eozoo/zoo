/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import java.util.Map;

import lombok.Data;

/**
 *
 * @author shanhuiming
 *
 */
@Data
public class ClusterInfo {

    private Integer id;

    private String name;

    private Integer level;

    private Map<String, Object> properties;
}
