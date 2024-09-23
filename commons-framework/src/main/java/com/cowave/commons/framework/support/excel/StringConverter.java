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
public class StringConverter implements Converter<Object> {

	public WriteCellData<String> convertToExcelData(WriteConverterContext<Object> context) {
		String value = "";
		Object object = context.getValue();
		if(object != null) {
			value = object.toString();
		}
		return new WriteCellData<>(value);
	}
}
