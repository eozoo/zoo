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
package com.cowave.zoo.framework.helper.rest;

import com.cowave.zoo.framework.configuration.ApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author shanhuiming
 *
 */
@Configuration(proxyBeanMethods = false)
public class HeaderInterceptorConfiguration {

    @Bean
    public HeaderInterceptor restHeaderInterceptor(
            @Value("${server.port:8080}") String port, ApplicationProperties applicationProperties) {
        return new HeaderInterceptor(port, applicationProperties.getClusterId());
    }

    @Order(0)
    @Bean
    public RestTemplateCustomizer restHeaderCustomizer(HeaderInterceptor interceptor) {
        return restTemplate -> {
            if (!restTemplate.getInterceptors().contains(interceptor)) {
                restTemplate.getInterceptors().add(interceptor);
            }
        };
    }
}
