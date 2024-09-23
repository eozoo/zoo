/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

/**
 *
 * @author shanhuiming
 *
 */
public class SuccessConverter implements Converter<Integer> {

    @Override
    public WriteCellData<String> convertToExcelData(WriteConverterContext<Integer> context) {
        Integer status = context.getValue();
        if(status != null) {
            if(status == 1) {
                return new WriteCellData<>("成功");
            }else if(status == 0) {
                return new WriteCellData<>("失败");
            }
        }
        return new WriteCellData<>("");
    }
}
