package com.cowave.commons.framework.helper.dict;

import com.cowave.commons.framework.support.redis.RedisHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass(RedisOperations.class)
@Configuration(proxyBeanMethods = false)
public class DictAutoConfiguration {

    @Bean
    public DictHelper dictHelper(RedisHelper redisHelper){
        return new DictHelper(redisHelper);
    }
}
