/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.filter.access;

import io.seata.core.context.RootContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 *
 * @author shanhuiming
 *
 */
@ConditionalOnClass({RootContext.class})
@Component
public class TransactionIdSetter {

    public void setXid(String xid){
        RootContext.bind(xid);
    }
}
