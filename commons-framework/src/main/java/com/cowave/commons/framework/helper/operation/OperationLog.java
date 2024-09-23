/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation;

import java.util.Map;

/**
 * 操作日志
 *
 * @author shanhuiming
 */
public interface OperationLog {

    int SUCCESS = 1;

    int FAIL = 0;

    /**
     * 是否异步处理
     */
    default boolean isAsync(){
        return true;
    }

    /**
     * 初始化日志属性
     */
    void initialize();

    /**
     * 日志类型
     */
    void setTypeCode(String typeCode);

    /**
     * 日志动作
     */
    void setActionCode(String actionCode);

    /**
     * 日志状态
     */
    void setLogStatus(Integer logStatus);

    /**
     * 日志描述
     */
    void setLogDesc(String logDesc);

    /**
     * 设置请求内容
     */
    void setRequest(Map<String, Object> request);

    /**
     * 设置响应内容
     */
    void setResponse(Object response);

    /**
     * 获取响应内容
     */
    Object getResponse();

    /**
     * 设置日志内容 key-value
     */
    void putContent(String key, Object obj);
}
