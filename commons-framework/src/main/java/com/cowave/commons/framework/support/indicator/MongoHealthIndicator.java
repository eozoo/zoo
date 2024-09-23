/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.indicator;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
@ConditionalOnClass(MongoTemplate.class)
@ConditionalOnEnabledHealthIndicator("mongo")
@AutoConfiguration(after = { MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
public class MongoHealthIndicator extends AbstractHealthIndicator {

    private final MongoTemplate mongoTemplate;

    private final MongoProperties mongoProperties;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        Document hostInfo = mongoTemplate.executeCommand("{ hostInfo: 1 }");
        Document system = hostInfo.get("system", Document.class);
        String host = system.getString("hostname");

        Document os = hostInfo.get("os", Document.class);
        String name = os.getString("type") + "_" + os.get("name") + "_" + os.get("version");

        Map<String, String> detail = new HashMap<>();
        detail.put("os", name);
        detail.put("host", host);
        detail.put("uri", mongoProperties.getUri());
        detail.put("database", mongoProperties.getDatabase());
        builder.up().withDetails(detail);
    }
}
