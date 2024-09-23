package com.cowave.commons.framework.configuration;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

/**
 *
 * @author shanhuiming
 *
 */
public class CowaveBranner implements Banner {

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println("  ______    ______  ____    __    ____  ___   ____    ____  _______");
        out.println(" /      |  /  __  \\ \\   \\  /  \\  /   / /   \\  \\   \\  /   / |   ____|");
        out.println("|  .----  |  |  |  | \\   \\/    \\/   / /  ^  \\  \\   \\/   /  |  |__");
        out.println("|  |      |  |  |  |  \\            / /  /_\\  \\  \\      /   |   __|");
        out.println("|  `----  |  `--'  |   \\    /\\    / /  _____  \\  \\    /    |  |____");
        out.println(" \\______|  \\______/     \\__/  \\__/ /__/     \\__\\  \\__/     |_______|");
    }
}
