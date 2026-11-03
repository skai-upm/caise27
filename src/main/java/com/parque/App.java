package com.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Entry point: reads commands from the terminal, one per line, and prints one response per command.
 */
public class App {
    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) throws IOException {
        run(System.in, System.out, USERS_FILE);
    }

    static void run(InputStream in, PrintStream out, Path usersFile) throws IOException {
        CommandProcessor processor = new CommandProcessor(new UserService(new UserRepository(usersFile)));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            out.println(processor.process(line));
        }
    }
}
