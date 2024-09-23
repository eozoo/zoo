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
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 *
 * @author shanhuiming
 *
 */
public class SexConverter implements Converter<Integer> {

	@Override
	public WriteCellData<String> convertToExcelData(WriteConverterContext<Integer> context) {
		Integer status = context.getValue();
		if (status != null) {
			if (status == 0) {
				return new WriteCellData<>("男");
			} else if (status == 1) {
				return new WriteCellData<>("女");
			} else if (status == 2) {
				return new WriteCellData<>("未知");
			}
		}
		return new WriteCellData<>("");
	}

	@Override
	public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
		String stringValue = cellData.getStringValue();
		if ("男".equals(stringValue)) {
			return 0;
		} else if ("女".equals(stringValue)) {
			return 1;
		} else if ("未知".equals(stringValue)) {
			return 2;
		}
		return null;
	}
}
