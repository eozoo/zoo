/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
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
        String info = "Copyright ©2017-06-30 Cowave All Rights Reserved";
        Resource resource = new DefaultResourceLoader().getResource("classpath:META-INF/git.info");
        if (resource.exists()) {
            try (InputStream input = resource.getInputStream()) {
                JSONObject json = JSON.parseObject(IOUtils.toString(input, StandardCharsets.UTF_8));
                String appName = (String) json.get("app.name");
                String appVersion = (String) json.get("app.version");
                String buildTime = (String) json.get("build.time");
                String gitId = (String) json.get("git.commit.id");
                String gitBranch = (String) json.get("git.branch");
                if(StringUtils.isBlank(gitBranch)){
                    info = appName + " " + appVersion + " build("+ buildTime + ")" + " " + info;
                }else{
                    info = appName + " " + appVersion + " build("+ buildTime + " " + gitBranch + " " + gitId + ")" + " " + info;
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
        out.println(info);
    }
}
