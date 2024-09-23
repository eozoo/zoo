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
import com.cowave.commons.framework.helper.operation.OperationAccepter;
import com.cowave.commons.framework.helper.operation.OperationAccepterConfiguration;
import com.cowave.commons.framework.helper.operation.OperationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class OperationKafkaAccepter<T extends OperationLog> implements OperationAccepter<T> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final OperationAccepterConfiguration accepterConfiguration;

    @Override
    public void accept(T log) {
        kafkaTemplate.send(accepterConfiguration.getKafkaTopic(), JSON.toJSONString(log));
    }
}
