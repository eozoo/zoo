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
package com.cowave.zoo.framework.helper.http.chooser;

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
