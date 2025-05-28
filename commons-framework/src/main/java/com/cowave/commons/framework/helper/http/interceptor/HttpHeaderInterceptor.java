/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.http.interceptor;

import com.cowave.commons.client.http.HttpClientInterceptor;
import com.cowave.commons.client.http.asserts.I18Messages;
import com.cowave.commons.client.http.request.HttpRequest;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.rest.interceptor.HeaderInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static com.cowave.commons.client.http.constants.HttpHeader.*;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@RequiredArgsConstructor
public class HttpHeaderInterceptor implements HttpClientInterceptor {

    private final String port;

    private final ApplicationProperties applicationProperties;

    @Override
    public void apply(HttpRequest httpRequest) {
        // X-Request-ID
        String accessId = Access.accessId();
        if (StringUtils.isBlank(accessId)) {
            accessId = HeaderInterceptor.newAccessId(port, applicationProperties);
        }
        httpRequest.header(X_Request_ID, accessId);

        // 常用请求头
        HttpServletRequest httpServletRequest = Access.httpRequest();
        if (httpServletRequest != null) {
            // Accept-Language
            String language = httpServletRequest.getHeader(Accept_Language);
            if (StringUtils.isBlank(language)) {
                language = I18Messages.getLanguage().getLanguage();
            }
            httpRequest.header(Accept_Language, language);

            // User-Agent
            String userAgent = httpServletRequest.getHeader(User_Agent);
            if (StringUtils.isBlank(userAgent)) {
                httpRequest.header(User_Agent, userAgent);
            }

            // X-Real-IP
            String xRealIp = httpServletRequest.getHeader(X_Real_IP);
            if (StringUtils.isBlank(userAgent)) {
                httpRequest.header(X_Real_IP, xRealIp);
            }

            // X-Forwarded-For
            String xForwardedFor = httpServletRequest.getHeader(X_Forwarded_For);
            if (StringUtils.isBlank(xForwardedFor)) {
                httpRequest.header(X_Forwarded_For, xForwardedFor);
            }

            // Proxy-Client-IP
            String proxyClientIp = httpServletRequest.getHeader(Proxy_Client_IP);
            if (StringUtils.isBlank(proxyClientIp)) {
                httpRequest.header(Proxy_Client_IP, proxyClientIp);
            }

            // WL-Proxy-Client-IP
            String wlProxyClientIp = httpServletRequest.getHeader(WL_Proxy_Client_IP);
            if (StringUtils.isBlank(wlProxyClientIp)) {
                httpRequest.header(WL_Proxy_Client_IP, wlProxyClientIp);
            }
        }
    }
}
