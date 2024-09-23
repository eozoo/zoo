/*
 * Copyright (c) 2017～2099 Cowave All Rights Reserved.
 *
 * For licensing information, please contact: https://www.cowave.com.
 *
 * This code is proprietary and confidential.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 */
import com.cowave.script.ScriptLogExtStream;
import com.cowave.script.ScriptLogStream;
import com.cowave.script.ScriptUtils;
import com.cowave.script.local.LocalScriptLogStream;

import java.io.IOException;

/**
 * @author jiangbo
 * @date 2024/5/29
 */
public class ScriptTest {

    public static void main(String[] args) throws IOException {
        ScriptUtils.Result result = ScriptUtils.exec("10.64.4.96", 22, "root", "cowave", "ping 10.64.4.96 -c 5;1+1;echo 'aa'");
        System.out.println("-----");

        ScriptUtils.Result result2 = ScriptUtils.exec("10.64.4.96", 22, "root", "cowave", "ping 10.64.4.96 -c 5;1+1;echo 'aa'",
                ScriptLogStream.apply(), ScriptLogExtStream.apply());
        System.out.println("-----");

        ScriptUtils.Result result3 = ScriptUtils.execByWindows("ping 10.64.4.96", LocalScriptLogStream.apply());
        System.out.println("-----");
    }

}
