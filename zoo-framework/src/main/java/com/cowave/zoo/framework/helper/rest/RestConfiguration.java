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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(RestProperties.class)
public class RestConfiguration {

    private final RestProperties restProperties;

    @ConditionalOnMissingBean(RestTemplate.class)
    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper, HttpClient httpClient){
        RestTemplate restTemplate = new RestTemplate();
        // 请求处理
        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));
        // 响应处理
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        for (int i = 0; i < messageConverters.size(); i++) {
            if (messageConverters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
                converter.setObjectMapper(objectMapper);
                messageConverters.set(i, converter);
            }
        }
        restTemplate.setMessageConverters(messageConverters);
        return restTemplate;
    }

    @ConditionalOnMissingBean(HttpClient.class)
    @Bean
    public HttpClient httpClient() {
        PoolingHttpClientConnectionManager connectionManager =
                PoolingHttpClientConnectionManagerBuilder.create()
                        .setMaxConnTotal(restProperties.getPoolConnections())
                        .setMaxConnPerRoute(500)
                        .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(restProperties.getConnectTimeout()))
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(restProperties.getPoolTimeout()))
                .setResponseTimeout(Timeout.ofMilliseconds(restProperties.getSocketTimeout()))
                .build();
        DefaultHttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy(
                restProperties.getRetryMax(),
                TimeValue.ofMilliseconds(restProperties.getRetryInterval()));
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryStrategy(retryStrategy)
                .build();
    }
}
