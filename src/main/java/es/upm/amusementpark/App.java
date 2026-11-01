package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard
 * input; with arguments the first one is the path of a command file.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application and returns the process exit status. */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path usersFile) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(usersFile)));
        } catch (IOException e) {
            err.println("ERROR: cannot load users from " + usersFile + ": " + e.getMessage());
            return 1;
        }
        if (args.length == 0) {
            return runInteractive(processor, in, out, err);
        }
        return runFile(processor, args[0], out, err);
    }

    private static int runInteractive(CommandProcessor processor, InputStream in, PrintStream out, PrintStream err) {
        try {
            processor.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandProcessor processor, String fileName, PrintStream out, PrintStream err) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(fileName), StandardCharsets.UTF_8)) {
            processor.run(reader, out);
            return 0;
        } catch (IOException | InvalidPathException e) {
            err.println("ERROR: cannot read command file '" + fileName + "': " + describe(e));
            return 1;
        }
    }

    private static String describe(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "file not found";
        }
        if (e instanceof AccessDeniedException) {
            return "permission denied";
        }
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
