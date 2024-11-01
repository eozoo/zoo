package com.cowave.commons.framework.helper.feign;

import com.cowave.commons.framework.access.AccessProperties;
import com.cowave.commons.framework.access.security.TokenService;
import com.cowave.commons.framework.configuration.ApplicationProperties;
import com.cowave.commons.framework.helper.feign.chooser.DefaultServiceChooser;
import com.cowave.commons.framework.helper.feign.chooser.EurekaServiceChooser;
import com.cowave.commons.framework.helper.feign.chooser.NacosServiceChooser;
import com.cowave.commons.framework.helper.feign.chooser.RedisServiceChooser;
import com.cowave.commons.framework.helper.feign.exception.FeignRollbackHandler;
import com.cowave.commons.framework.helper.feign.interceptor.FeignSeataInterceptor;
import feign.RequestInterceptor;
import io.seata.core.context.RootContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.feign.FeignExceptionHandler;
import org.springframework.feign.FeignServiceChooser;
import org.springframework.feign.annotation.EnableFeign;
import org.springframework.feign.invoke.FeignSyncInvoker;

import javax.annotation.Nullable;

/**
 *
 * @author shanhuiming
 *
 */
@EnableFeign
@ConditionalOnClass({FeignSyncInvoker.class, RootContext.class})
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
public class FeignSeataConfiguration {

    @Nullable
    private final RedisServiceChooser redisServiceChooser;

    @Nullable
    private final EurekaServiceChooser eurekaServiceChooser;

    @Nullable
    private final NacosServiceChooser nacosServiceChooser;

    @Nullable
    private final TokenService tokenService;

    @ConditionalOnMissingBean(FeignServiceChooser.class)
    @Bean
    public FeignServiceChooser feignServiceChooser(){
        return new DefaultServiceChooser(redisServiceChooser, eurekaServiceChooser, nacosServiceChooser);
    }

    @Bean
    public RequestInterceptor requestInterceptor(@Value("${server.port:8080}") String port,
            ApplicationProperties applicationProperties, AccessProperties accessProperties) {
        return new FeignSeataInterceptor(port, tokenService, accessProperties, applicationProperties);
    }

    @ConditionalOnMissingBean(FeignExceptionHandler.class)
    @Bean
    public FeignRollbackHandler feignRollbackHandler(){
        return new FeignRollbackHandler();
    }
}
