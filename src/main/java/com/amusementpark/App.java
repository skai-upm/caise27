package com.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Starts the application and reads commands from standard input or, when a path
 * is given as the first argument, from that file.
 */
public class App {
    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        System.exit(run(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, USERS_FILE));
    }

    /**
     * Runs the application and returns its exit status.
     */
    static int run(String[] args, BufferedReader stdin, PrintStream out, Path usersFile) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(usersFile)), out);
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: could not load users from " + usersFile + ": " + e.getMessage());
            return 1;
        }
        if (args.length == 0) {
            out.println("Amusement park - type HELP for the list of commands");
            return processAll(stdin, processor, out, "standard input");
        }
        try (BufferedReader file = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            return processAll(file, processor, out, args[0]);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: cannot open command file " + args[0] + ": " + describe(e));
            return 1;
        }
    }

    private static int processAll(BufferedReader reader, CommandProcessor processor,
                                  PrintStream out, String source) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!processor.process(line)) {
                    break;
                }
            }
            return 0;
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: cannot read " + source + ": " + describe(e));
            return 1;
        }
    }

    private static String describe(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "file does not exist";
        }
        if (e instanceof AccessDeniedException) {
            return "permission denied";
        }
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
}
