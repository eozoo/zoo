/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation.kafka;

import com.cowave.commons.framework.configuration.AccessConfiguration;
import com.cowave.commons.framework.helper.operation.OperationHandler;
import com.cowave.commons.framework.helper.operation.OperationParser;
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

    @ConditionalOnProperty(name = "spring.access.oplog.kafka-enable", matchIfMissing = true)
    @ConditionalOnMissingBean(OperationParser.class)
    @Bean
    public OperationHandler<? super OperationLog> operationService(
            KafkaTemplate<String, Object> kafkaTemplate, AccessConfiguration accessConfiguration){
        return new OperationKafkaHandler<>(kafkaTemplate, accessConfiguration.getOplog());
    }
}
