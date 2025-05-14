package com.cowave.commons.framework.access;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class MissingAdviceCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory == null) {
            return true;
        }

        String[] beanNames = beanFactory.getBeanDefinitionNames();
        for (String name : beanNames) {
            BeanDefinition bd = beanFactory.getBeanDefinition(name);
            String className = bd.getBeanClassName();
            if (className != null) {
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(RestControllerAdvice.class)
                            || clazz.isAnnotationPresent(ControllerAdvice.class)) {
                    }
                } catch (ClassNotFoundException e) {
                    log.error("", e);
                }
            }
        }
        return true;
    }
}
