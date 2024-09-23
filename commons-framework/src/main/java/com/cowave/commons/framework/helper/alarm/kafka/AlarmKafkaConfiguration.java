/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.alarm.kafka;

import com.cowave.commons.framework.helper.alarm.Alarm;
import com.cowave.commons.framework.helper.alarm.AlarmAccepter;
import com.cowave.commons.framework.helper.alarm.AlarmAccepterConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(KafkaTemplate.class)
@Configuration(proxyBeanMethods = false)
public class AlarmKafkaConfiguration {

    @ConditionalOnProperty(name = "spring.application.alarm.kafka-enable", matchIfMissing = true)
    @ConditionalOnMissingBean(AlarmAccepter.class)
    @Bean
    public AlarmAccepter<? super Alarm> alarmService(
            KafkaTemplate<String, Object> kafkaTemplate, AlarmAccepterConfiguration accepterConfiguration){
        return new AlarmKafkaAccepter<>(kafkaTemplate, accepterConfiguration);
    }
}
