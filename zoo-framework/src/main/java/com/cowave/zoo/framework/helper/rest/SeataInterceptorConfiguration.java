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

import io.seata.core.context.RootContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.core.annotation.Order;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({RootContext.class})
@Configuration(proxyBeanMethods = false)
public class SeataInterceptorConfiguration {

    @Bean
    public SeataInterceptor restSeataInterceptor() {
        return new SeataInterceptor();
    }

    @Order(1)
    @Bean
    public RestTemplateCustomizer restSeataCustomizer(SeataInterceptor interceptor) {
        return restTemplate -> {
            if (!restTemplate.getInterceptors().contains(interceptor)) {
                restTemplate.getInterceptors().add(interceptor);
            }
        };
    }
}
