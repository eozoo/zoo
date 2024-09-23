/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.feign.chooser;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.feign.FeignServiceChooser;
import org.springframework.feign.annotation.FeignClient;

import javax.annotation.Nullable;


/**
*
* @author shanhuiming
*
*/
@ConditionalOnClass({FeignClient.class})
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class ServiceChooserConfiguration {

    @Nullable
    private final RedisServiceChooser redisServiceChooser;

    @Nullable
    private final EurekaServiceChooser eurekaServiceChooser;

    @Nullable
    private final NacosServiceChooser nacosServiceChooser;

    @ConditionalOnMissingBean(FeignServiceChooser.class)
    @Bean
    public FeignServiceChooser feignServiceChooser(){
        return new DefaultServiceChooser(redisServiceChooser, eurekaServiceChooser, nacosServiceChooser);
    }
}
