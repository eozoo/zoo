/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.kafka;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;

/**
 * @author shanhuiming
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@AutoConfigureBefore(KafkaAutoConfiguration.class)
public class KafkaMultiConfiguration {

    @Conditional(MultiPrivateKafkaCondition.class)
    @Primary
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> privateKafkaListenerContainerFactory(Environment environment) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.private", KafkaProperties.class).get();
        checkAuthProperties(properties);
        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Conditional(MultiPrivateKafkaCondition.class)
    @Primary
    @Bean
    public ProducerFactory<?, ?> privateKafkaProducerFactory(Environment environment,
                                                             ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.private", KafkaProperties.class).get();
        checkAuthProperties(properties);
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                properties.buildProducerProperties());
        String transactionIdPrefix = properties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Conditional(MultiPrivateKafkaCondition.class)
    @Primary
    @Bean
    public KafkaTemplate<?, ?> privateKafkaTemplate(Environment environment, @Qualifier("privateKafkaProducerFactory") ProducerFactory<Object, Object> kafkaProducerFactory,
                                                    ProducerListener<Object, Object> kafkaProducerListener, ObjectProvider<RecordMessageConverter> messageConverter) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.private", KafkaProperties.class).get();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        map.from(kafkaProducerListener).to(kafkaTemplate::setProducerListener);
        map.from(properties.getTemplate().getDefaultTopic()).to(kafkaTemplate::setDefaultTopic);
        map.from(properties.getTemplate().getTransactionIdPrefix()).to(kafkaTemplate::setTransactionIdPrefix);
        return kafkaTemplate;
    }

    @Conditional(MultiPrivateKafkaCondition.class)
    @Primary
    @Bean
    public KafkaAdmin privateKafkaAdmin(Environment environment) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.private", KafkaProperties.class).get();
        checkAuthProperties(properties);
        KafkaAdmin kafkaAdmin = new KafkaAdmin(properties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(properties.getAdmin().isFailFast());
        return kafkaAdmin;
    }

    @Conditional(MultiPublicKafkaCondition.class)
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> publicKafkaListenerContainerFactory(Environment environment) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.public", KafkaProperties.class).get();
        checkAuthProperties(properties);
        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Conditional(MultiPublicKafkaCondition.class)
    @Bean
    public ProducerFactory<?, ?> publicKafkaProducerFactory(Environment environment,
                                                             ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.public", KafkaProperties.class).get();
        checkAuthProperties(properties);
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(
                properties.buildProducerProperties());
        String transactionIdPrefix = properties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        return factory;
    }

    @Conditional(MultiPublicKafkaCondition.class)
    @Bean
    public KafkaTemplate<?, ?> publicKafkaTemplate(Environment environment, @Qualifier("publicKafkaProducerFactory") ProducerFactory<Object, Object> kafkaProducerFactory,
                                                    ProducerListener<Object, Object> kafkaProducerListener, ObjectProvider<RecordMessageConverter> messageConverter) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.public", KafkaProperties.class).get();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        map.from(kafkaProducerListener).to(kafkaTemplate::setProducerListener);
        map.from(properties.getTemplate().getDefaultTopic()).to(kafkaTemplate::setDefaultTopic);
        map.from(properties.getTemplate().getTransactionIdPrefix()).to(kafkaTemplate::setTransactionIdPrefix);
        return kafkaTemplate;
    }

    @Conditional(MultiPublicKafkaCondition.class)
    @Bean
    public KafkaAdmin publicKafkaAdmin(Environment environment) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.public", KafkaProperties.class).get();
        checkAuthProperties(properties);
        KafkaAdmin kafkaAdmin = new KafkaAdmin(properties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(properties.getAdmin().isFailFast());
        return kafkaAdmin;
    }

    private void checkAuthProperties(KafkaProperties properties) {
        String userName = properties.getProperties().get("username");
        if (StringUtils.isBlank(userName)) {
            properties.getProperties().clear();
        }
    }
}
