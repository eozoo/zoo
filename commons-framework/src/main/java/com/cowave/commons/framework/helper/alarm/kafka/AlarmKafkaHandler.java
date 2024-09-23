/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.alarm.kafka;

import com.alibaba.fastjson.JSON;

import com.cowave.commons.framework.helper.alarm.Alarm;
import com.cowave.commons.framework.helper.alarm.AlarmHandler;
import com.cowave.commons.framework.helper.alarm.AlarmHandlerConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class AlarmKafkaHandler<T extends Alarm> implements AlarmHandler<T> {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final AlarmHandlerConfiguration accepterConfiguration;

    @Override
    public void handle(T alarm) {
        kafkaTemplate.send(accepterConfiguration.getKafkaTopic(), JSON.toJSONString(alarm));
    }
}
