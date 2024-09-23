package com.cowave.commons.framework.support.redis.connection;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 *
 * @author shanhuiming
 *
 */
public class MultiOriginRedisCondition implements Condition {

	@Override
	public boolean matches(ConditionContext context, @NotNull AnnotatedTypeMetadata metadata) {
		RedisProperties redis = Binder.get(context.getEnvironment()).bind(
				"spring.redis", RedisProperties.class).orElse(null);
		return redis != null;
	}
}
