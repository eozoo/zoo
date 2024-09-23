package com.cowave.commons.framework.support.datasource;

import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(AbstractDataSource.class)
@Aspect
@Order(1)
@Component
public class DataSourceAspect{

    @Pointcut("@annotation(com.cowave.commons.framework.support.datasource.DataSource) || @within(com.cowave.commons.framework.support.datasource.DataSource)")
    public void dsPointCut(){

    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable{
        DataSource dataSource = getDataSource(point);
        if (ObjectUtils.isNotEmpty(dataSource)){
        	DynamicDataSource.set(dataSource.value());
        }
        try{
            return point.proceed();
        }finally{
        	DynamicDataSource.clear();
        }
    }

    public DataSource getDataSource(ProceedingJoinPoint point){
        MethodSignature signature = (MethodSignature) point.getSignature();
        DataSource dataSource = AnnotationUtils.findAnnotation(signature.getMethod(), DataSource.class);
        if (Objects.nonNull(dataSource)){
            return dataSource;
        }
        return AnnotationUtils.findAnnotation(signature.getDeclaringType(), DataSource.class);
    }
}
