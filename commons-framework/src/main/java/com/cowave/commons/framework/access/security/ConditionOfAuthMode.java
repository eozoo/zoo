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

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
public class ConditionOfAuthMode implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> attrs = metadata.getAnnotationAttributes(ConditionalOnHasUrls.class.getName());
        String mode = (String) attrs.get("value");
        String key = "spring.access.auth." + mode + "-urls";
        String camelKey = "spring.access.auth." + toCamel(mode) + "Urls";

        Binder binder = Binder.get(context.getEnvironment());
        try {
            List<String> urls = binder.bind(key, List.class).orElseGet(
                    () -> binder.bind(camelKey, List.class).orElse(Collections.emptyList()));
            return !urls.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private String toCamel(String mode) {
        StringBuilder sb = new StringBuilder();
        boolean upper = false;
        for (char c : mode.toCharArray()) {
            if (c == '-') {
                upper = true;
            } else {
                sb.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return sb.toString();
    }
}
