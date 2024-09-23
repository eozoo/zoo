package com.cowave.commons.framework.support.feign.chooser;

import lombok.RequiredArgsConstructor;
import org.springframework.feign.FeignServiceChooser;

/**
 *
 * @author shanhuiming
 *
 */
@RequiredArgsConstructor
public class DefaultServiceChooser implements FeignServiceChooser {

    private final RedisServiceChooser redisServiceChooser;

    private final EurekaServiceChooser eurekaServiceChooser;

    private final NacosServiceChooser nacosServiceChooser;

    @Override
    public String choose(String name) {
        String serviceUrl = null;
        if(redisServiceChooser != null){
            serviceUrl = redisServiceChooser.choose(name);
        }
        if(serviceUrl == null && eurekaServiceChooser != null){
            serviceUrl = eurekaServiceChooser.choose(name);
        }
        if(serviceUrl == null && nacosServiceChooser != null){
            serviceUrl = nacosServiceChooser.choose(name);
        }
        return serviceUrl;
    }
}
