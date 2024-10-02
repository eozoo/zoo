/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation.kafka;

import com.alibaba.fastjson.JSON;
import com.cowave.commons.framework.helper.operation.OperationHandler;
import com.cowave.commons.framework.helper.operation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class OperationKafkaHandler<T extends OperationLog> implements OperationHandler<T> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String kafkaTopic;

    @Override
    public void handle(T log) {
        kafkaTemplate.send(kafkaTopic, JSON.toJSONString(log));
    }
}
