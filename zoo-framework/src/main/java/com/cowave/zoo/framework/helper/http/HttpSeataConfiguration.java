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
package com.cowave.zoo.framework.helper.http;

import com.cowave.zoo.framework.helper.http.fallback.DefaultHttpFallback;
import com.cowave.zoo.http.client.HttpFallback;
import com.cowave.zoo.http.client.invoke.proxy.HttpMethodInvoker;
import com.cowave.zoo.framework.helper.http.interceptor.HttpSeataInterceptor;
import io.seata.core.context.RootContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({HttpMethodInvoker.class, RootContext.class})
@Configuration(proxyBeanMethods = false)
public class HttpSeataConfiguration {

    @ConditionalOnMissingBean(HttpFallback.class)
    @Bean
    public DefaultHttpFallback httpFallback() {
        return new DefaultHttpFallback();
    }

    @Bean
    public HttpSeataInterceptor httpSeataInterceptor() {
        return new HttpSeataInterceptor();
    }
}
