package com.cowave.commons.tools;/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
import com.cowave.commons.tools.ssh.output.LoggerExtOutputStream;
import com.cowave.commons.tools.ssh.output.LoggerOutputStream;
import com.cowave.commons.tools.ssh.Terminal;

import java.io.IOException;

/**
 * @author jiangbo
 */
public class TerminalTest {

    public static void main(String[] args) throws IOException {
        Terminal.Result result = Terminal.execRemote("10.64.4.96", 22, "root", "cowave", "ping 10.64.4.96 -c 5;1+1;echo 'aa'");

        Terminal.Result result2 = Terminal.execRemote("10.64.4.96", 22, "root", "cowave", "ping 10.64.4.96 -c 5;1+1;echo 'aa'",
                LoggerOutputStream.apply(), LoggerExtOutputStream.apply());
    }
}
