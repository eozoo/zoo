/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.operation;

import lombok.Data;

import java.util.Date;

/**
 * 操作属性
 *
 * @author shanhuiming
 */
@Data
public class OperationInfo {

    /**
     * accessTime
     */
    private Date accessTime;

    /**
     * accessIp
     */
    private String accessIp;

    /**
     * accessUrl
     */
    private String accessUrl;

    /**
     * userId
     */
    private Long userId;

    /**
     * userCode
     */
    private String userCode;

    /**
     * deptId
     */
    private Long deptId;

    /**
     * deptCode
     */
    private String deptCode;

    /**
     * 操作类型
     */
    private String opType;

    /**
     * 操作动作
     */
    private String opAction;

    /**
     * 操作是否成功
     */
    private boolean success;
}
