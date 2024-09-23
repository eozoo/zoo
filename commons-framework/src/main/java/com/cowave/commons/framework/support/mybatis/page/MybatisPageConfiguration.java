package com.cowave.commons.framework.support.mybatis.page;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.cowave.jackson.JacksonUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author jiangbo
 *
 */
@ConditionalOnClass(PaginationInnerInterceptor.class)
@Configuration
public class MybatisPageConfiguration {

    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        return new MybatisPlusInterceptor();
    }

    @Bean
    public PaginationInnerInterceptor paginationInnerInterceptor(MybatisPlusInterceptor mybatisPlusInterceptor) {
        JacksonTypeHandler.setObjectMapper(JacksonUtils.objectMapper());
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor);
        return paginationInnerInterceptor;
    }
}
