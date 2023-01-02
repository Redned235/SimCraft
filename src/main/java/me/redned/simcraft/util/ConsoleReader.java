package me.redned.simcraft.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ConsoleReader {

    public static String readFromConsole(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        InputStream reader = process.getInputStream();
        process.waitFor();

        return new String(reader.readAllBytes(), StandardCharsets.UTF_8);
    }
}