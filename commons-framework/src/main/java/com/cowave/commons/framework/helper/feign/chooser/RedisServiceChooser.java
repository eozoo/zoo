/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.feign.chooser;

import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.redis.RedisHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({RedisOperations.class})
@RequiredArgsConstructor
@Component
public class RedisServiceChooser {

    public static final String PREFIX_ROUTE = "route:";

    private final SecureRandom random = new SecureRandom();

    private final ApplicationProperties applicationProperties;

    private final RedisHelper redis;

    public String choose(String name) {
        // 按集群区分
        Map<String, Set<String>> cachedRoute = redis.getValue(PREFIX_ROUTE + applicationProperties.getClusterId());
        if(cachedRoute == null){
            return null;
        }

        Set<String> routes = cachedRoute.get(name);
        if(routes == null || routes.isEmpty()){
            return null;
        }

        List<String> list = new ArrayList<>(routes);
        if(list.size() == 1){
            return list.get(0);
        }
        return list.get(random.nextInt(routes.size()));
    }
}
