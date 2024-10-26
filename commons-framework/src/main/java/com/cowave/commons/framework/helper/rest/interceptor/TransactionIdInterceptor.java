/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.rest.interceptor;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessConfiguration;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.access.security.TokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@RequiredArgsConstructor
public class TransactionIdInterceptor implements RequestInterceptor, ClientHttpRequestInterceptor {

    private final String port;

    private final TokenService tokenService;

    private final AccessConfiguration accessConfiguration;

    private final ApplicationProperties applicationProperties;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String accessId = Access.accessId();
        String authorization = Access.accessToken();
        String xid = RootContext.getXID(); // Seata事务id
        if(StringUtils.isBlank(accessId)) {
            accessId = HeaderInterceptor.newAccessId(port, applicationProperties);
            log.debug(">< new access-id: {}", accessId);
             requestTemplate.header("Access-Id", accessId);
        }
        if(StringUtils.isBlank(authorization) && tokenService != null) {
            authorization = HeaderInterceptor.newAuthorization(tokenService, applicationProperties);
            requestTemplate.header(accessConfiguration.tokenHeader(), authorization);
        }
        if(StringUtils.isNotBlank(xid)){
            requestTemplate.header(RootContext.KEY_XID, xid);
        }
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String accessId = Access.accessId();
        String authorization = Access.accessToken();
        String xid = RootContext.getXID(); // Seata事务id
        if(StringUtils.isBlank(accessId)) {
            accessId = HeaderInterceptor.newAccessId(port, applicationProperties);
            log.debug(">< new access-id: {}", accessId);
            request.getHeaders().add("Access-Id", accessId);
        }
        if(StringUtils.isBlank(authorization) && tokenService != null) {
            authorization = HeaderInterceptor.newAuthorization(tokenService, applicationProperties);
            request.getHeaders().add(accessConfiguration.tokenHeader(), authorization);
        }
        if(StringUtils.isNotBlank(xid)){
            request.getHeaders().add(RootContext.KEY_XID, xid);
        }
        return execution.execute(request, body);
    }
}
