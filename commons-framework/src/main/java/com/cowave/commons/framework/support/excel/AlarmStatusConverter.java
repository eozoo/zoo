package com.cowave.commons.framework.support.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

/**
 * 
 * @author shanhuiming
 * 
 */
public class AlarmStatusConverter implements Converter<Integer> {

	public WriteCellData<String> convertToExcelData(WriteConverterContext<Integer> context) {
		String value = ""; 
		Integer status = context.getValue();
		if(status != null) {
		    if(status == 0) {
                value = "未处理"; 
            }else if(status == 1) {
				value = "确认"; 
			}else if(status == 2) {
				value = "误报";
			}
		}
		return new WriteCellData<>(value);
	}
}
