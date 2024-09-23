/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation.kafka;

import com.cowave.commons.framework.helper.operation.OperationAccepter;
import com.cowave.commons.framework.helper.operation.OperationAccepterConfiguration;
import com.cowave.commons.framework.helper.operation.OperationLog;
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
public class OperationKafkaConfiguration {

    @ConditionalOnProperty(name = "spring.application.oplog.kafka-enable", matchIfMissing = true)
    @ConditionalOnMissingBean(OperationAccepter.class)
    @Bean
    public OperationAccepter<? super OperationLog> operationService(
            KafkaTemplate<String, Object> kafkaTemplate, OperationAccepterConfiguration accepterConfiguration){
        return new OperationKafkaAccepter<>(kafkaTemplate, accepterConfiguration);
    }
}
