package com.cowave.commons.framework.support.excel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;

import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.Head;
import com.alibaba.excel.metadata.data.CellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.style.column.AbstractColumnWidthStyleStrategy;

/**
 *
 * @author shanhuiming
 *
 */
public class CellWidthHandler extends AbstractColumnWidthStyleStrategy {

    private Integer fixedWidth;

    private final Map<Integer, Map<Integer, Integer>> sheetCache;

    public CellWidthHandler() {
        this.sheetCache = new HashMap<>();
    }

    public CellWidthHandler(Integer fixedWidth) {
        this.fixedWidth = fixedWidth;
        this.sheetCache = new HashMap<>();
    }

    @Override
    protected void setColumnWidth(WriteSheetHolder writeSheetHolder,
    		List<WriteCellData<?>> cellDataList, Cell cell, Head head, Integer relativeRowIndex, Boolean isHead) {
    	if(relativeRowIndex > 0) {
    		return;
    	}

        if(fixedWidth!=null){
            int columnWidth = this.fixedWidth;
            if (columnWidth > 100) {
                columnWidth = 100;
            }
            writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), columnWidth * 280);
        }else {
            boolean needSetWidth = isHead || !CollectionUtils.isEmpty(cellDataList);
            if (needSetWidth) {
                Map<Integer, Integer> maxColumnWidthMap = sheetCache.computeIfAbsent(writeSheetHolder.getSheetNo(), k -> new HashMap<>());
                Integer columnWidth = this.dataLength(cellDataList, cell, isHead);
                if (columnWidth >= 0) {
                    if (columnWidth > 100) {
                        columnWidth = 100;
                    }

                    Integer maxColumnWidth = maxColumnWidthMap.get(cell.getColumnIndex());
                    if (maxColumnWidth == null || columnWidth > maxColumnWidth) {
                        maxColumnWidthMap.put(cell.getColumnIndex(), columnWidth);
                        writeSheetHolder.getSheet().setColumnWidth(cell.getColumnIndex(), columnWidth * 280);
                    }
                }
            }
        }
    }

    private Integer dataLength(List<WriteCellData<?>> cellDataList, Cell cell, boolean isHead) {
        if (isHead) {
            return cell.getStringCellValue().getBytes().length;
        } else {
            CellData<?> cellData = cellDataList.get(0);
            CellDataTypeEnum type = cellData.getType();
            if (type == null) {
                return -1;
            } else {
                return switch (type) {
                    case STRING -> cellData.getStringValue().getBytes().length;
                    case BOOLEAN -> cellData.getBooleanValue().toString().getBytes().length;
                    case NUMBER -> cellData.getNumberValue().toString().getBytes().length;
                    default -> -1;
                };
            }
        }
    }
}
