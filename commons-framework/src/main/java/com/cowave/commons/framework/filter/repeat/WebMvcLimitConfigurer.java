package com.cowave.commons.framework.filter.repeat;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class WebMvcLimitConfigurer implements WebMvcConfigurer{

    private final AbstractRepeatLimitInterceptor repeatSubmitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
    }
}
