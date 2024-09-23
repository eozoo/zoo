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
