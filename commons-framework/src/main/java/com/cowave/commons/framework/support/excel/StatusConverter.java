package com.cowave.commons.framework.support.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.CellData;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

/**
 *
 * @author shanhuiming
 *
 */
public class StatusConverter implements Converter<Integer> {

	@Override
	public WriteCellData<String> convertToExcelData(WriteConverterContext<Integer> context) {
		Integer status = context.getValue();
		if(status != null) {
			if(status == 1) {
				return new WriteCellData<>("启用");
			}else if(status == 0) {
				return new WriteCellData<>("停用");
			}
		}
		return new WriteCellData<>("");
	}

	@Override
	public Integer convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
		String stringValue = cellData.getStringValue();
		if("启用".equals(stringValue)){
			return 1;
		}
		if("停用".equals(stringValue)){
			return 0;
		}
		return null;
	}
}
