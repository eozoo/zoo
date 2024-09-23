package com.cowave.commons.framework.support.kafka;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/**
 *
 * @author shanhuiming
 *
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
        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Conditional(MultiPrivateKafkaCondition.class)
    @Primary
    @Bean
    public KafkaTemplate<?, ?> privateKafkaTemplate(Environment environment) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.private", KafkaProperties.class).get();
        ProducerFactory<?, ?> producerFactory = new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
        return new KafkaTemplate<>(producerFactory);
    }

    @Conditional(MultiPrivateKafkaCondition.class)
    @Primary
    @Bean
    public KafkaAdmin privateKafkaAdmin(Environment environment){
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.private", KafkaProperties.class).get();
        KafkaAdmin kafkaAdmin = new KafkaAdmin(properties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(properties.getAdmin().isFailFast());
        return kafkaAdmin;
    }

    @Conditional(MultiPublicKafkaCondition.class)
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> publicKafkaListenerContainerFactory(Environment environment) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.public", KafkaProperties.class).get();
        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(properties.buildConsumerProperties());
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Conditional(MultiPublicKafkaCondition.class)
    @Bean
    public KafkaTemplate<?, ?> publicKafkaTemplate(Environment environment) {
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.public", KafkaProperties.class).get();
        ProducerFactory<?, ?> producerFactory = new DefaultKafkaProducerFactory<>(properties.buildProducerProperties());
        return new KafkaTemplate<>(producerFactory);
    }

    @Conditional(MultiPublicKafkaCondition.class)
    @Bean
    public KafkaAdmin publicKafkaAdmin(Environment environment){
        KafkaProperties properties = Binder.get(environment).bind("spring.kafka.public", KafkaProperties.class).get();
        KafkaAdmin kafkaAdmin = new KafkaAdmin(properties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(properties.getAdmin().isFailFast());
        return kafkaAdmin;
    }
}
