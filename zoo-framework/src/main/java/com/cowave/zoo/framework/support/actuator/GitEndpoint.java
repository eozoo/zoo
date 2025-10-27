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
package com.cowave.zoo.framework.support.actuator;

import com.alibaba.fastjson.JSON;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointDiscoverer;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({EndpointDiscoverer.class})
@Endpoint(id = "git")
@Component
public class GitEndpoint {

    @ReadOperation
    public SortedMap<String, Object> info() throws IOException {
        Resource resource = new DefaultResourceLoader().getResource("classpath:META-INF/git.info");
        if (resource.exists()) {
            try (InputStream input = resource.getInputStream()) {
                return JSON.parseObject(IOUtils.toString(input, StandardCharsets.UTF_8), TreeMap.class);
            }
        }
        return new TreeMap<>();
    }
}
