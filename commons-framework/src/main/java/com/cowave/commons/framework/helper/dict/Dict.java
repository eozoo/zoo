/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.helper.dict;

/**
 * 字典
 *
 * @author shanhuiming
 */
public interface Dict {

    /**
     * 字典分组
     */
    String getGroupCode();

    /**
     * 字典类型
     */
    String getTypeCode();

    /**
     * 字典码
     */
    String getDictCode();

    /**
     * 字典名称
     */
    String getDictLabel();

    /**
     * 字典值
     */
    Object getDictValue();

    /**
     * 字典排序
     */
    Integer getDictOrder();
}
