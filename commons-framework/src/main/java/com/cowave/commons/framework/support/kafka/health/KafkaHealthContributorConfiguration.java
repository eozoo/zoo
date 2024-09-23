package com.cowave.commons.framework.support.kafka.health;

import java.util.Map;

import com.cowave.commons.framework.support.kafka.KafkaMultiConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnEnabledHealthIndicator("kafka")
@AutoConfiguration(after = { KafkaMultiConfiguration.class })
public class KafkaHealthContributorConfiguration
        extends CompositeHealthContributorConfiguration<KafkaHealthIndicator, KafkaAdmin> {

    @Bean
    @ConditionalOnMissingBean(name = { "kafkaHealthIndicator" })
    public HealthContributor kafkaHealthContributor(Map<String, KafkaAdmin> kafkaMap) {
        return createContributor(kafkaMap);
    }
}
