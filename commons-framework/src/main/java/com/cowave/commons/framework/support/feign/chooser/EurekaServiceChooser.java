package com.cowave.commons.framework.support.feign.chooser;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({LoadBalancerClient.class, EurekaAutoServiceRegistration.class})
@RequiredArgsConstructor
@Component
public class EurekaServiceChooser {

    @Nullable
    private final LoadBalancerClient balancerClient;

    public String choose(String name) {
        if(balancerClient == null){
            return null;
        }

        ServiceInstance instance = balancerClient.choose(name.toUpperCase());
        if (instance == null) {
            throw new IllegalArgumentException("service[" + name + "] not exist");
        }
        return instance.getUri().toString();
    }
}
