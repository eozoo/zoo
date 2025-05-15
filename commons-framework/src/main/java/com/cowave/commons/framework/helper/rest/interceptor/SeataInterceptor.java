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

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

import static com.cowave.commons.client.http.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@RequiredArgsConstructor
public class SeataInterceptor implements ClientHttpRequestInterceptor {

    private final String port;

    private final ApplicationProperties applicationProperties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // X-Request-ID
        String accessId = Access.accessId();
        if(StringUtils.isBlank(accessId)) {
            accessId = HeaderInterceptor.newAccessId(port, applicationProperties);
        }
        request.getHeaders().add(X_Request_ID, accessId);

        // Seata事务id
        String xid = RootContext.getXID();
        if(StringUtils.isNotBlank(xid)){
            request.getHeaders().add(RootContext.KEY_XID, xid);
        }

        // 其它header
        HttpServletRequest httpServletRequest = Access.httpRequest();
        if (httpServletRequest != null) {
            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = httpServletRequest.getHeader(name);
                request.getHeaders().add(name, value);
            }
        }
        return execution.execute(request, body);
    }
}
