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
import com.cowave.commons.client.http.request.HttpRequest;
import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.rest.interceptor.HeaderInterceptor;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

import static com.cowave.commons.client.http.constants.HttpHeader.X_Request_ID;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@RequiredArgsConstructor
public class HttpSeataInterceptor implements HttpClientInterceptor {

    private final String port;

    private final ApplicationProperties applicationProperties;

    @Override
    public void apply(HttpRequest httpRequest) {
        // X-Request-ID
        String accessId = Access.accessId();
        if(StringUtils.isBlank(accessId)) {
            accessId = HeaderInterceptor.newAccessId(port, applicationProperties);
        }
        httpRequest.header(X_Request_ID, accessId);

        // Seata事务id
        String xid = RootContext.getXID();
        if(StringUtils.isNotBlank(xid)){
            httpRequest.header(RootContext.KEY_XID, xid);
        }

        // 其它header
        HttpServletRequest httpServletRequest = Access.httpRequest();
        if (httpServletRequest != null) {
            Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                String value = httpServletRequest.getHeader(name);
                httpRequest.header(name, value);
            }
        }
    }
}
