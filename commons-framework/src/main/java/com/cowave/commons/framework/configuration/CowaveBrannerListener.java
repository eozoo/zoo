/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.commons.framework.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class CowaveBrannerListener implements SpringApplicationRunListener {

    private final SpringApplication springApplication;

    private final String[] args;

    public CowaveBrannerListener(){
        this.springApplication = null;
        this.args = null;
    }

    public CowaveBrannerListener(SpringApplication springApplication, String[] args){
        this.springApplication = springApplication;
        this.args = args;
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        if(springApplication != null){
            springApplication.setBanner(new CowaveBranner());
        }
    }
}
