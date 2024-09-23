/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.indicator;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.*;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@ConditionalOnClass(DiscoveryClient.class)
@ConditionalOnEnabledHealthIndicator("application")
@AutoConfiguration
public class ApplicationHealthIndicator extends AbstractHealthIndicator {

    private final DiscoveryClient discoveryClient;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Map<String, List<String>> table = new TreeMap<>();
        List<String> services = discoveryClient.getServices();
        for(String name : services){
            List<String> list = table.computeIfAbsent(name, k -> new ArrayList<>());
            List<ServiceInstance> instances = discoveryClient.getInstances(name);
            for(ServiceInstance instance : instances){
                list.add(instance.getUri().toString());
            }
        }
        builder.up().withDetails(table);
    }
}
