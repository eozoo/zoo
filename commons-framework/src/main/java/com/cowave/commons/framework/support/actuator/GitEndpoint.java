/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.actuator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
    public JSONObject info() throws IOException {
        Resource resource = new DefaultResourceLoader().getResource("classpath:META-INF/git.info");
        if (resource.exists()) {
            try (InputStream input = resource.getInputStream()) {
                return JSON.parseObject(IOUtils.toString(input, StandardCharsets.UTF_8));
            }
        }
        return new JSONObject();
    }
}
