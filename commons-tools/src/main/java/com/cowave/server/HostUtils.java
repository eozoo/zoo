/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.server;

import com.cowave.script.ScriptUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * @author jiangbo
 * @date 2023/12/13
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostUtils {

    public static List<String> getLocalIpv4s() throws SocketException {
        List<String> ips = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(':') == -1) {
                    ips.add(address.getHostAddress());
                }
            }
        }
        return ips;
    }

    public static List<Integer> getLocalPorts() throws IOException, InterruptedException {
        String os = System.getenv("OS");
        if (StringUtils.isNotBlank(os) && StringUtils.containsIgnoreCase(os, "windows")) {
            return List.of();
        }
        String cmd = "netstat -nltp |awk '{if (NR>2){print $4}}'| awk -F \":\" '{print $NF}' | sort | uniq";
        String[] cmds = {"/bin/bash", "-c", cmd};
        ScriptUtils.Result result = ScriptUtils.runProcess(cmds, Map.of());
        if(!result.isSuccess()){
            throw new UnsupportedEncodingException("获取本地所有端口失败：" + result.getError());
        }
        return Arrays.stream(StringUtils.split(result.getOutput(), "\n"))
                .map(Integer::parseInt).toList();
    }

}
