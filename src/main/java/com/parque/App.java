package com.parque;

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
 * Entry point. Without arguments commands are read from standard input; with an argument the first one
 * is the path of a command file. Both sources go through the same loop and the same CommandProcessor.
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
     * Runs the application and returns the process exit status (0 normal end, 1 controlled failure).
     */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path usersFile) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserService(new UserRepository(usersFile)));
        } catch (IOException e) {
            err.println("ERROR: Cannot load users from '" + usersFile + "': " + describe(e));
            return 1;
        }
        if (args.length == 0) {
            try {
                processLines(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out, processor);
            } catch (IOException e) {
                err.println("ERROR: Cannot read standard input: " + describe(e));
                return 1;
            }
            return 0;
        }
        String commandFile = args[0];
        try (BufferedReader reader = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            processLines(reader, out, processor);
        } catch (IOException | InvalidPathException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        return 0;
    }

    /** The single command loop shared by interactive and file mode. */
    private static void processLines(BufferedReader reader, PrintStream out, CommandProcessor processor)
            throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            out.println(processor.process(line));
        }
    }

    private static String describe(Exception e) {
        String kind = e.getClass().getSimpleName();
        return e.getMessage() == null ? kind : kind + " (" + e.getMessage() + ")";
    }
}
