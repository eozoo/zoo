/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.rest.interceptor;

import com.cowave.commons.client.http.asserts.I18Messages;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.tools.ids.IdGenerator;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.cowave.commons.client.http.constants.HttpHeader.*;
import static com.cowave.commons.client.http.constants.HttpHeader.WL_Proxy_Client_IP;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class HeaderInterceptor implements ClientHttpRequestInterceptor {

    private static final IdGenerator GENERATOR = new IdGenerator();

    private final String port;

    private final ApplicationProperties applicationProperties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // X-Request-ID
        String accessId = Access.accessId();
        if(StringUtils.isBlank(accessId)) {
            accessId = newAccessId(port, applicationProperties);
        }
        request.getHeaders().add(X_Request_ID, accessId);

        // 常用请求头
        HttpServletRequest httpServletRequest = Access.httpRequest();
        if (httpServletRequest != null) {
            // Accept-Language
            String language = httpServletRequest.getHeader(Accept_Language);
            if (StringUtils.isBlank(language)) {
                language = I18Messages.getLanguage().getLanguage();
            }
            request.getHeaders().add(Accept_Language, language);

            // User-Agent
            String userAgent = httpServletRequest.getHeader(User_Agent);
            if (StringUtils.isBlank(userAgent)) {
                request.getHeaders().add(User_Agent, userAgent);
            }

            // X-Real-IP
            String xRealIp = httpServletRequest.getHeader(X_Real_IP);
            if (StringUtils.isBlank(userAgent)) {
                request.getHeaders().add(X_Real_IP, xRealIp);
            }

            // X-Forwarded-For
            String xForwardedFor = httpServletRequest.getHeader(X_Forwarded_For);
            if (StringUtils.isBlank(xForwardedFor)) {
                request.getHeaders().add(X_Forwarded_For, xForwardedFor);
            }

            // Proxy-Client-IP
            String proxyClientIp = httpServletRequest.getHeader(Proxy_Client_IP);
            if (StringUtils.isBlank(proxyClientIp)) {
                request.getHeaders().add(Proxy_Client_IP, proxyClientIp);
            }

            // WL-Proxy-Client-IP
            String wlProxyClientIp = httpServletRequest.getHeader(WL_Proxy_Client_IP);
            if (StringUtils.isBlank(wlProxyClientIp)) {
                request.getHeaders().add(WL_Proxy_Client_IP, wlProxyClientIp);
            }
        }
        return execution.execute(request, body);
    }

    public static String newAccessId(String port, ApplicationProperties applicationProperties) {
        String prefix = "#" + applicationProperties.getClusterId() + port;
        return GENERATOR.generateIdWithDate(prefix, "", "yyyyMMddHHmmss", 1000);
    }
}
