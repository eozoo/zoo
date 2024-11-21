/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.feign.interceptor;

import com.cowave.commons.framework.access.Access;
import com.cowave.commons.framework.access.AccessProperties;
import com.cowave.commons.framework.access.security.BearerTokenService;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.rest.interceptor.HeaderInterceptor;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
@RequiredArgsConstructor
public class FeignHeaderInterceptor implements RequestInterceptor {

    private final String port;

    private final BearerTokenService bearerTokenService;

    private final AccessProperties accessProperties;

    private final ApplicationProperties applicationProperties;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        // Header Access-Id
        String accessId = Access.accessId();
        if (StringUtils.isBlank(accessId)) {
            accessId = HeaderInterceptor.newAccessId(port, applicationProperties);
            log.debug(">< new access-id: {}", accessId);
        }
        requestTemplate.header("X-Request-ID", accessId);

        // Header Token
        if (!requestTemplate.headers().containsKey(accessProperties.tokenKey())) {
            String authorization = Access.accessToken();
            if (StringUtils.isNotBlank(authorization)) {
                requestTemplate.header(accessProperties.tokenKey(), authorization);
            } else if (bearerTokenService != null) {
                authorization = HeaderInterceptor.newAuthorization(bearerTokenService, applicationProperties);
                requestTemplate.header(accessProperties.tokenKey(), authorization);
            }
        }
    }
}
