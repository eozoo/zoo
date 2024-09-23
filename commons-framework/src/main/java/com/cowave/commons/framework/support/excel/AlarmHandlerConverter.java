package com.cowave.commons.framework.support.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

/**
 * 
 * @author shanhuiming
 * 
 */
public class AlarmHandlerConverter implements Converter<Integer> {

    public WriteCellData<String> convertToExcelData(WriteConverterContext<Integer> context) {
        String value = "";  
        Integer status = context.getValue();
        if(status != null) {
            if(status == 1) {
                value = "手动"; 
            }else if(status == 2) {
                value = "自动";
            }
        }
        return new WriteCellData<>(value);
    }
}
