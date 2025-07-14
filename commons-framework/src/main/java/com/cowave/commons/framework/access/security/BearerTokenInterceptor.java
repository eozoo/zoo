/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.access.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;

/**
 * @author shanhuiming
 */
public interface BearerTokenInterceptor {

    /**
     * AccessToken填充字段
     */
    default void additionalAccessClaims(JwtBuilder jwtBuilder) {

    }

    /**
     * RefreshToken填充字段
     */
    default void additionalRefreshClaims(JwtBuilder jwtBuilder) {

    }

    /**
     * 解析AccessToken
     */
    default void additionalParseAccessToken(Claims claims, AccessUserDetails userDetails) {

    }
}
