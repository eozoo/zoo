package com.cowave.commons.framework.filter.repeat;

import com.cowave.commons.framework.support.redis.RedisHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 *
 * @author shanhuiming
 *
 */
public class RepeatLimitConfiguration {

    @ConditionalOnBean(RedisHelper.class)
    @Bean
    public RepeatUrlDataLimiter sameUrlDataInterceptorAbstract(RedisHelper redisHelper){
        return new RepeatUrlDataLimiter(redisHelper);
    }

    @ConditionalOnBean(AbstractRepeatLimitInterceptor.class)
    @Bean
    public WebMvcLimitConfigurer webMvcLimitConfigurer(AbstractRepeatLimitInterceptor limitInterceptor){
        return new WebMvcLimitConfigurer(limitInterceptor);
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
    @ConditionalOnBean(WebMvcLimitConfigurer.class)
    @Bean
    public FilterRegistrationBean someFilterRegistration(){
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new RepeatLimitFilter());
        registration.addUrlPatterns("/*");
        registration.setName("repeatableFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 50);
        return registration;
    }
}
