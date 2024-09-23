/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.feign.chooser;

import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({LoadBalancerClient.class, NacosAutoServiceRegistration.class})
@RequiredArgsConstructor
@Component
public class NacosServiceChooser {

    @Nullable
    private final LoadBalancerClient balancerClient;

    public String choose(String name) {
        if(balancerClient == null){
            return null;
        }

        ServiceInstance instance = balancerClient.choose(name);
        if (instance == null) {
            throw new IllegalArgumentException("service[" + name + "] not exist");
        }
        return instance.getUri().toString();
    }
}
