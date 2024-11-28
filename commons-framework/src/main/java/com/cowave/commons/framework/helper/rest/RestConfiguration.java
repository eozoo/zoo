/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.net.SocketException;
import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
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
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();
        // 连接池
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        // 连接池最大连接数
        connectionManager.setMaxTotal(restProperties.getMaxConnections());
        // 路由是对maxTotal的细分
        connectionManager.setDefaultMaxPerRoute(500);
        // 返回数据等待时间
        connectionManager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(restProperties.getSocketTimeout()).build());

        // 连接设置
        RequestConfig requestConfig = RequestConfig.custom()
                // 连接服务等待时间
                .setConnectTimeout(restProperties.getConnectTimeout())
                // 连接池获取连接等待时间
                .setConnectionRequestTimeout(restProperties.getPoolTimeout()).build();

        // 重试次数
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);
        httpClientBuilder.setConnectionManager(connectionManager);
        httpClientBuilder.setRetryHandler((exception, execCount, context) -> {
            if (execCount > restProperties.getMaxRetry()) {
                return false;
            }

            try {
                Thread.sleep( execCount * restProperties.getRetryInterval());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }

            // io异常触发重试
            if (exception instanceof ConnectTimeoutException
                    || exception instanceof NoHttpResponseException
                    || exception instanceof SocketException) {
                if (context instanceof HttpClientContext) {
                    HttpRequest request = (HttpRequest) context.getAttribute(HttpClientContext.HTTP_REQUEST);
                    if (request != null) {
                        String url = request.getRequestLine().getUri();
                        log.warn("Retry[" + execCount + "] of rest, url=" + url);
                    }
                }
                return true;
            }
            return false;
        });
        return httpClientBuilder.build();
    }
}
