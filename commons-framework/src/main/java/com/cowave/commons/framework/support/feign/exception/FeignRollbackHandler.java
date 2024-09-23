/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.feign.exception;

import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.feign.FeignExceptionHandler;
import org.springframework.feign.invoke.RemoteException;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class FeignRollbackHandler implements FeignExceptionHandler {

    @Override
    public void handle(RemoteException e) {
        String xid = RootContext.getXID();
        if(StringUtils.isNotBlank(xid)){
            try {
                GlobalTransactionContext.reload(xid).rollback();
            } catch (TransactionException ex) {
                log.error("Rollback faile[" + xid + "]", ex);
            }
        }
    }
}
