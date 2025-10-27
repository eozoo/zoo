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
package com.cowave.zoo.framework.helper.kafka.health;

import java.util.Map;

import com.cowave.zoo.framework.helper.kafka.KafkaAutoConfiguration;
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
@AutoConfiguration(after = { KafkaAutoConfiguration.class })
public class KafkaHealthContributorConfiguration
        extends CompositeHealthContributorConfiguration<KafkaHealthIndicator, KafkaAdmin> {

    @Bean
    @ConditionalOnMissingBean(name = { "kafkaHealthIndicator" })
    public HealthContributor kafkaHealthContributor(Map<String, KafkaAdmin> kafkaMap) {
        return createContributor(kafkaMap);
    }
}
