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
