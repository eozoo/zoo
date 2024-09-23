package com.cowave.commons.framework.support.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.fastjson.JSON;

/**
 *
 * @author shanhuiming
 *
 */
public class JsonConverter implements Converter<Object> {

	public WriteCellData<String> convertToExcelData(WriteConverterContext<Object> context) throws Exception {
		String value = "{}";
		Object obj = context.getValue();
		if(obj != null) {
			value = JSON.toJSONString(obj);
		}
		return new WriteCellData<>(value);
	}
}
