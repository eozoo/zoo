/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.tools;

/**
 *
 * @author shanhuiming
 *
 */
public class AssertsException extends RuntimeException {

    private transient Object[] args;

    private boolean language;

	public AssertsException(String message) {
        super(message);
    }

    public AssertsException(String message, Throwable cause) {
        super(message, cause);
    }

    public AssertsException language(boolean language){
        this.language = language;
        return this;
    }

    public AssertsException args(Object... args){
        this.args = args;
        return this;
    }

    public boolean getLanguage(){
        return this.language;
    }

    public Object[] getArgs(){
        return this.args;
    }
}
