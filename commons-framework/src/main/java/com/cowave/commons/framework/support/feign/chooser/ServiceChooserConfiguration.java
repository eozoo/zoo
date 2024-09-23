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
