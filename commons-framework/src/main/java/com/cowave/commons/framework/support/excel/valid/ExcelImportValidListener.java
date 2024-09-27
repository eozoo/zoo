/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.support.excel.valid;

import cn.hutool.extra.validation.BeanValidationResult;
import cn.hutool.extra.validation.ValidationUtil;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.cowave.commons.tools.Messages;
import com.cowave.commons.tools.AssertsException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 触发@ExcelProperty标记的属性上的Valid校验
 *
 * @author shanhuiming
 */
public class ExcelImportValidListener<T> extends AnalysisEventListener<T> {
    private final List<T> list = new ArrayList<>();
    private final ExcelDataImporter<T> dataImporter;
    private final boolean overwrite;

    public ExcelImportValidListener(ExcelDataImporter<T> dataImporter, boolean overwrite){
        this.dataImporter = dataImporter;
        this.overwrite = overwrite;
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        valid(data, context);
        list.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        dataImporter.importExcelData(list, overwrite);
    }

    private void valid(Object bean, AnalysisContext context) {
        StringBuilder failedBuilder = new StringBuilder(Messages.msg("frame.import.failed.msg"));
        Field[] fields = bean.getClass().getDeclaredFields();
        boolean validFailed = false;
        for (Field field : fields) {
            field.setAccessible(true);
            ExcelProperty excel = field.getAnnotation(ExcelProperty.class);
            if(excel == null){
                continue;
            }
            BeanValidationResult validationResult = ValidationUtil.warpValidateProperty(bean, field.getName());
            if (!validationResult.isSuccess()) {
                validFailed = true;
                failedBuilder.append("<br/>").append(getMessage(context.readRowHolder().getRowIndex() + 1, validationResult.getErrorMessages().get(0).getMessage()));
            }
        }
        if(validFailed){
            throw new AssertsException(failedBuilder.toString());
        }
    }

    private String getMessage(int rowIndex, String message){
        return Messages.msg("frame.excel.failed.row", rowIndex, message);
    }
}
