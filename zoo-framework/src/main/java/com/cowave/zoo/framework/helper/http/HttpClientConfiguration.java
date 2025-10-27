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

import com.cowave.zoo.http.client.HttpClientInterceptor;
import com.cowave.zoo.http.client.HttpServiceChooser;
import com.cowave.zoo.http.client.invoke.proxy.HttpMethodInvoker;
import com.cowave.zoo.framework.configuration.ApplicationProperties;
import com.cowave.zoo.framework.helper.http.chooser.DefaultServiceChooser;
import com.cowave.zoo.framework.helper.http.chooser.EurekaServiceChooser;
import com.cowave.zoo.framework.helper.http.chooser.NacosServiceChooser;
import com.cowave.zoo.framework.helper.http.interceptor.HttpHeaderInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnMissingClass("io.seata.core.context.RootContext")
@ConditionalOnClass({HttpMethodInvoker.class})
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class HttpClientConfiguration {

    @Nullable
    private final EurekaServiceChooser eurekaServiceChooser;

    @Nullable
    private final NacosServiceChooser nacosServiceChooser;

    @ConditionalOnMissingBean(HttpServiceChooser.class)
    @Bean
    public HttpServiceChooser httpServiceChooser(){
        return new DefaultServiceChooser(eurekaServiceChooser, nacosServiceChooser);
    }

    @Bean
    public HttpClientInterceptor httpClientInterceptor(
            @Value("${server.port:8080}") String port, ApplicationProperties applicationProperties) {
        return new HttpHeaderInterceptor(port, applicationProperties);
    }
}
