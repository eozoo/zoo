/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.helper.kafka;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.kafka.support.converter.RecordMessageConverter;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(KafkaTemplate.class)
@AutoConfigureBefore(org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class)
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaAutoConfiguration {

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> kafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
        String username = checkSecurityProperties(kafkaProperties);
        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        if (StringUtils.isNotBlank(username)) {
            kafkaProperties.getProperties().put("username", username);
        }
        return factory;
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public ProducerFactory<?, ?> producerFactory(KafkaProperties kafkaProperties, ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        String username = checkSecurityProperties(kafkaProperties);
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
        String transactionIdPrefix = kafkaProperties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        if (StringUtils.isNotBlank(username)) {
            kafkaProperties.getProperties().put("username", username);
        }
        return factory;
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public KafkaTemplate<?, ?> kafkaTemplate(KafkaProperties kafkaProperties, ProducerFactory<Object, Object> kafkaProducerFactory,
                                             ProducerListener<Object, Object> kafkaProducerListener, ObjectProvider<RecordMessageConverter> messageConverter) {
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(kafkaProducerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(kafkaProducerListener).to(kafkaTemplate::setProducerListener);
        propertyMapper.from(kafkaProperties.getTemplate().getDefaultTopic()).to(kafkaTemplate::setDefaultTopic);
        propertyMapper.from(kafkaProperties.getTemplate().getTransactionIdPrefix()).to(kafkaTemplate::setTransactionIdPrefix);
        return kafkaTemplate;
    }

    @ConditionalOnMissingBean
    @Primary
    @Bean
    public KafkaAdmin kafkaAdmin(KafkaProperties kafkaProperties) {
        String username = checkSecurityProperties(kafkaProperties);
        KafkaAdmin kafkaAdmin = new KafkaAdmin(kafkaProperties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(kafkaProperties.getAdmin().isFailFast());
        if (StringUtils.isNotBlank(username)) {
            kafkaProperties.getProperties().put("username", username);
        }
        return kafkaAdmin;
    }

    @Conditional(ZooKafkaCondition.class)
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>> zooKafkaListenerContainerFactory(Environment environment) {
        KafkaProperties kafkaProperties = Binder.get(environment).bind("zoo.kafka", KafkaProperties.class).get();
        String username = checkSecurityProperties(kafkaProperties);
        ConsumerFactory<String, Object> consumerFactory = new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        if (StringUtils.isNotBlank(username)) {
            kafkaProperties.getProperties().put("username", username);
        }
        return factory;
    }

    @Conditional(ZooKafkaCondition.class)
    @Bean
    public ProducerFactory<?, ?> zooProducerFactory(Environment environment, ObjectProvider<DefaultKafkaProducerFactoryCustomizer> customizers) {
        KafkaProperties kafkaProperties = Binder.get(environment).bind("zoo.kafka", KafkaProperties.class).get();
        String username = checkSecurityProperties(kafkaProperties);
        DefaultKafkaProducerFactory<?, ?> factory = new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
        String transactionIdPrefix = kafkaProperties.getProducer().getTransactionIdPrefix();
        if (transactionIdPrefix != null) {
            factory.setTransactionIdPrefix(transactionIdPrefix);
        }
        customizers.orderedStream().forEach(customizer -> customizer.customize(factory));
        if (StringUtils.isNotBlank(username)) {
            kafkaProperties.getProperties().put("username", username);
        }
        return factory;
    }

    @Conditional(ZooKafkaCondition.class)
    @Bean
    public KafkaTemplate<?, ?> zooKafkaTemplate(Environment environment, @Qualifier("zooProducerFactory") ProducerFactory<Object, Object> producerFactory,
                                                   ProducerListener<Object, Object> kafkaProducerListener, ObjectProvider<RecordMessageConverter> messageConverter) {
        KafkaProperties kafkaProperties = Binder.get(environment).bind("zoo.kafka", KafkaProperties.class).get();
        KafkaTemplate<Object, Object> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        messageConverter.ifUnique(kafkaTemplate::setMessageConverter);
        PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
        propertyMapper.from(kafkaProducerListener).to(kafkaTemplate::setProducerListener);
        propertyMapper.from(kafkaProperties.getTemplate().getDefaultTopic()).to(kafkaTemplate::setDefaultTopic);
        propertyMapper.from(kafkaProperties.getTemplate().getTransactionIdPrefix()).to(kafkaTemplate::setTransactionIdPrefix);
        return kafkaTemplate;
    }

    @Conditional(ZooKafkaCondition.class)
    @Bean
    public KafkaAdmin zooKafkaAdmin(Environment environment) {
        KafkaProperties kafkaProperties = Binder.get(environment).bind("zoo.kafka", KafkaProperties.class).get();
        String username = checkSecurityProperties(kafkaProperties);
        KafkaAdmin kafkaAdmin = new KafkaAdmin(kafkaProperties.buildAdminProperties());
        kafkaAdmin.setFatalIfBrokerNotAvailable(kafkaProperties.getAdmin().isFailFast());
        if (StringUtils.isNotBlank(username)) {
            kafkaProperties.getProperties().put("username", username);
        }
        return kafkaAdmin;
    }

    // 场景：properties配置了鉴权，但是用户名密码为空会导致报错，这里偷懒使用一个username来标记用户名，为空就去掉鉴权配置项
    private String checkSecurityProperties(KafkaProperties kafkaProperties) {
        String userName = kafkaProperties.getProperties().remove("username");
        if (StringUtils.isBlank(userName)) {
            kafkaProperties.getProperties().clear();
        }
        return userName;
    }
}
