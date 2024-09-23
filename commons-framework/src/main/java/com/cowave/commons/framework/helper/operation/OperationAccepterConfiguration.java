package com.cowave.commons.framework.helper.operation;

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
@ConfigurationProperties(prefix = "spring.application.oplog")
public class OperationAccepterConfiguration {

    private boolean kafkaEnable = true;

    private String kafkaTopic = "sys-oplog";

}
