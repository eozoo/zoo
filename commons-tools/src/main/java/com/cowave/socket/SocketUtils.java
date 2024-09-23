/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
package com.cowave.socket;

import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author jiangbo
 * @date 2023/12/26
 */
public class SocketUtils {

    /**
     * ip+port是否可用
     */
    public static boolean socketValidate(String ip, Integer port) {
        return socketValidate(ip, port, 3 * 1000);
    }

    public static boolean socketValidate(String ip, Integer port, int timeout) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), timeout);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
