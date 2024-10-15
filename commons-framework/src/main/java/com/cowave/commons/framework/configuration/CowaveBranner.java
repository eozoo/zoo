/*
 * Copyright (c) 2017～2024 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.configuration;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author shanhuiming
 *
 */
@Slf4j
public class CowaveBranner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        String info = "";
        Resource resource = new DefaultResourceLoader().getResource("classpath:META-INF/git.info");
        if (resource.exists()) {
            try (InputStream input = resource.getInputStream()) {
                JSONObject json = JSON.parseObject(IOUtils.toString(input, StandardCharsets.UTF_8));
                String appName = (String) json.get("app.name");
                String appVersion = (String) json.get("app.version");
                String buildTime = (String) json.get("build.time");
                String gitId = (String) json.get("git.commit.id");
                String gitBranch = (String) json.get("git.branch");
                if(StringUtils.isNotBlank(appName) && StringUtils.isNotBlank(gitBranch)){
                    info = appName + " " + appVersion + " build(" + buildTime + " " + gitBranch + " " + gitId + ")";
                }else if(StringUtils.isNotBlank(appName)){
                    info = appName + " " + appVersion + " build(" + buildTime + ")";
                }else if(StringUtils.isNotBlank(gitBranch)){
                    info = "build(" + gitBranch + " " + gitId + ")";
                }
            }catch(Exception e){
                log.error("", e);
            }
        }
        out.println("  ______    ______  ____    __    ____  ___   ____    ____  _______");
        out.println(" /      |  /  __  \\ \\   \\  /  \\  /   / /   \\  \\   \\  /   / |   ____|");
        out.println("|  .----  |  |  |  | \\   \\/    \\/   / /  ^  \\  \\   \\/   /  |  |__");
        out.println("|  |      |  |  |  |  \\            / /  /_\\  \\  \\      /   |   __|");
        out.println("|  `----  |  `--'  |   \\    /\\    / /  _____  \\  \\    /    |  |____");
        out.println(" \\______|  \\______/     \\__/  \\__/ /__/     \\__\\  \\__/     |_______|");
        if(StringUtils.isNotBlank(info)){
            out.println(info);
        }
    }
}
