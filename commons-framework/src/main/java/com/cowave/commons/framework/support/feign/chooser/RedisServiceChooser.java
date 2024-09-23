package com.cowave.commons.framework.support.feign.chooser;

import com.cowave.commons.framework.configuration.ClusterInfo;
import com.cowave.commons.framework.support.redis.RedisHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({RedisOperations.class})
@RequiredArgsConstructor
@Component
public class RedisServiceChooser {

    public static final String PREFIX_ROUTE = "route:";

    private final SecureRandom random = new SecureRandom();

    private final ClusterInfo clusterInfo;

    private final RedisHelper redis;

    public String choose(String name) {
        // 按集群区分
        Map<String, Set<String>> cachedRoute = redis.getValue(PREFIX_ROUTE + clusterInfo.getId());
        if(cachedRoute == null){
            return null;
        }

        Set<String> routes = cachedRoute.get(name);
        if(routes == null || routes.isEmpty()){
            return null;
        }

        List<String> list = new ArrayList<>(routes);
        if(list.size() == 1){
            return list.get(0);
        }
        return list.get(random.nextInt(routes.size()));
    }
}
