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
