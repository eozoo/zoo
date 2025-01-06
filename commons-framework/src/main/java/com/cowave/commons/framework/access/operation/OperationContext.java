package com.cowave.commons.framework.access.operation;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class OperationContext {

    private static final ThreadLocal<OperationInfo> HOLDER = new TransmittableThreadLocal<>();

    public static void prepareContent(Object content){
        OperationInfo operationInfo = HOLDER.get();
        if(operationInfo == null){
            log.warn("operation not prepared");
            return;
        }
        operationInfo.setOpContent(content);
    }

    static void set(OperationInfo operationInfo){
        HOLDER.set(operationInfo);
    }

    static OperationInfo get(){
        return HOLDER.get();
    }

    static void remove(){
        HOLDER.remove();
    }
}
