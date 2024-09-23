package com.cowave.commons.framework.support.kafka;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *
 * @author shanhuiming
 *
 */
public class MultiPrivateKafkaCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		return Binder.get(context.getEnvironment()).bind(
				"spring.kafka.private", KafkaProperties.class).orElse(null) != null;
	}
}
