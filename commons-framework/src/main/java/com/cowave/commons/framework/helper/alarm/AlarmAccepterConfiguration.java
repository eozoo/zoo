package com.cowave.commons.framework.helper.alarm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author shanhuiming
 *
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.application.alarm")
public class AlarmAccepterConfiguration {

    private boolean kafkaEnable = true;

    private String kafkaTopic = "sys-alarm";
}
