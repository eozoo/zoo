/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.helper.http.fallback;

import com.cowave.zoo.http.client.HttpFallback;
import com.cowave.zoo.http.client.response.HttpResponse;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;

/**
 * @author shanhuiming
 */
@Slf4j
public class DefaultHttpFallback implements HttpFallback {

    @Override
    public void fallback(Method method, Object[] args, HttpResponse<?> response, Throwable cause) {
        String xid = RootContext.getXID();
        if (StringUtils.isNotBlank(xid)) {
            try {
                GlobalTransactionContext.reload(xid).rollback();
            } catch (TransactionException exception) {
                log.error("Rollback failed[" + xid + "]", exception);
            }
        }
    }
}
