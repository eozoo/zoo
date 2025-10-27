/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.zoo.framework.configuration;

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
public class ZooBannerListener implements SpringApplicationRunListener {

    private final SpringApplication springApplication;

    public ZooBannerListener(SpringApplication springApplication, String[] args){
        this.springApplication = springApplication;
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        if(springApplication != null){
            springApplication.setBanner(new ZooBanner());
        }
    }
}
