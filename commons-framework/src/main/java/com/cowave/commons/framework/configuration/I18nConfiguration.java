/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import javax.validation.Validator;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 *
 * @author shanhuiming
 *
 */
@AutoConfigureBefore({MessageSourceAutoConfiguration.class})
public class I18nConfiguration {

    private static final String FRAMEWORK_MESSAGES = "META-INF/i18n/messages-frame";

    @Bean
    public ResourceBundleMessageSource messageSource(@Value("${spring.messages.basename:}") String messages) {
        String[] array;
        if(StringUtils.isNotBlank(messages)){
            array = Stream.concat(
                    Arrays.stream(messages.split(",")), Stream.of(FRAMEWORK_MESSAGES)).toArray(String[]::new);
        }else{
            array = new String[]{FRAMEWORK_MESSAGES};
        }
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasenames(array);
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean(MessageSource messageSource) {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setProviderClass(HibernateValidator.class);
        localValidatorFactoryBean.setValidationMessageSource(messageSource);
        return localValidatorFactoryBean;
    }

    @Bean
    public Validator validator(LocalValidatorFactoryBean localValidatorFactoryBean) {
        return localValidatorFactoryBean.getValidator();
    }
}
