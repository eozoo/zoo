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
package com.cowave.zoo.framework.access.security;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author shanhuiming
 *
 */
public enum AuthMode {

    /**
     * Basic认证
     */
    BASIC("basic"),

    /**
     * Access Token认证
     */
    ACCESS("access"),

    /**
     * Access-Refresh Token认证
     */
    ACCESS_REFRESH("access-refresh");

    private final String value;

    AuthMode(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
