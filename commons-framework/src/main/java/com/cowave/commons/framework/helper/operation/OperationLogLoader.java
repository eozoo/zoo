package com.cowave.commons.framework.helper.operation;

import org.reflections.Reflections;

import java.util.Set;

/**
 *
 * @author shanhuiming
 *
 */
public class OperationLogLoader {

    private static Class<? extends OperationLog> operationClass;

    static {
        Reflections reflections = new Reflections("com.cowave");
        Set<Class<? extends OperationLog>> classes = reflections.getSubTypesOf(OperationLog.class);
        if(classes.isEmpty()){
            throw new UnsupportedOperationException("No implementation found for OperationLog");
        }
        if(classes.size() > 1){
            throw new UnsupportedOperationException("find more than one implementation of OperationLog, which is ambiguous");
        }
        for(Class<? extends OperationLog> clazz : classes){
            operationClass = clazz;
        }

        try {
            operationClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }
    }

    static OperationLog newLog() {
        try{
            return operationClass.getDeclaredConstructor().newInstance();
        }catch (Exception e) {
            // never will happen
            throw new UnsupportedOperationException(e);
        }
    }
}
