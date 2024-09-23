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
