/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.excel;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;

/**
 * 
 * @author shanhuiming
 * 
 */
public class DateConverter implements Converter<Date> {
	
	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss"; 

	public WriteCellData<String> convertToExcelData(WriteConverterContext<Date> context) {
		String value = ""; 
		Date date = context.getValue();
		if(date != null) {
			value = new SimpleDateFormat(FORMAT).format(date);
		}
		return new WriteCellData<>(value);
	}
}
