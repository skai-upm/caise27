package com.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard input;
 * with an argument, the first argument is the path of a command file.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with the given streams and users file.
     *
     * @return 0 on normal termination, 1 if users or the command file could not be read
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path usersFile) {
        UserManager userManager;
        try {
            userManager = new UserManager(new UserFileStorage(usersFile));
        } catch (IOException e) {
            err.println("ERROR: Could not read users file " + usersFile + ": " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = new CommandProcessor(userManager, out);
        if (args.length == 0) {
            return runInteractive(processor, in, err);
        }
        return runFile(processor, args[0], err);
    }

    private static int runInteractive(CommandProcessor processor, InputStream in, PrintStream err) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        try {
            processor.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: Could not read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandProcessor processor, String fileName, PrintStream err) {
        Path path;
        try {
            path = Path.of(fileName);
        } catch (InvalidPathException e) {
            err.println("ERROR: Invalid command file path: " + fileName);
            return 1;
        }
        if (!Files.isRegularFile(path)) {
            err.println("ERROR: Command file not found: " + fileName);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            processor.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: Could not read command file " + fileName + ": " + e.getMessage());
            return 1;
        }
    }
}
