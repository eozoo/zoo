package com.cowave.commons.framework.support.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

/**
 * 
 * @author shanhuiming
 * 
 */
public class AlarmLevelConverter implements Converter<Integer> {

	// 1:提示 2:普通 3:重要 4:严重'
	public WriteCellData<String> convertToExcelData(WriteConverterContext<Integer> context) {
		String value = "提示";  
		Integer status = context.getValue();
		if(status != null) {
			if(status == 2) {
				value = "普通"; 
			}else if(status == 3) {
				value = "重要";
			}else if(status == 4) {
				value = "严重";
			}
		}
		return new WriteCellData<>(value);
	}
}
