/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.excel.write;

import com.alibaba.excel.write.handler.SheetWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
public class DropdownWriteHandler implements SheetWriteHandler {

    private final int cellIndex;

    private final int rowSize;

    private final List<String> list;

    public DropdownWriteHandler(List<String> list, int cellIndex, int rowSize) {
        this.list = list;
        this.rowSize = rowSize;
        this.cellIndex = cellIndex;
    }

    @Override
    public void afterSheetCreate(WriteWorkbookHolder writeWorkbookHolder, WriteSheetHolder writeSheetHolder) {
        Sheet sheet = writeSheetHolder.getSheet();
        DataValidationHelper helper = sheet.getDataValidationHelper();
        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, rowSize, cellIndex, cellIndex);
        DataValidationConstraint constraint = helper.createExplicitListConstraint(list.toArray(new String[]{}));

        DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);
        dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
        dataValidation.setShowErrorBox(true);
        dataValidation.setSuppressDropDownArrow(true);
        dataValidation.createErrorBox("数据错误", "非法数据");
        sheet.addValidationData(dataValidation);
    }
}
