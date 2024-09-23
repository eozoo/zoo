package com.cowave.commons.framework.support.excel.valid;

import java.util.List;

/**
 *
 * @author shanhuiming
 *
 */
public interface ExcelDataImporter<T> {

    /**
     * 导入Excel数据
     */
    void importExcelData(List<T> list, boolean overwrite);
}
