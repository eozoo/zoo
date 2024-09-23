/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
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
public class AlarmHandlerConfiguration {

    private boolean kafkaEnable = true;

    private String kafkaTopic = "sys-alarm";
}
