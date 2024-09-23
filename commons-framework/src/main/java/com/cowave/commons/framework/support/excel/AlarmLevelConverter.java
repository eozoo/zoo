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
