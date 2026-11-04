package es.upm.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard input;
 * with an argument, the first one is the path of a command file. Both modes share
 * the same {@link CommandRunner}.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, USERS_FILE, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application and returns its exit status (0 on success). */
    static int run(String[] args, Path usersFile, InputStream stdin, PrintStream out, PrintStream err) {
        CommandRunner runner;
        try {
            runner = new CommandRunner(new CommandProcessor(new UserManager(new UserFileStorage(usersFile))));
        } catch (UncheckedIOException e) {
            err.println("ERROR: cannot load users file '" + usersFile + "': " + e.getCause().getMessage());
            return 1;
        }
        if (args.length == 0) {
            return runInteractive(runner, stdin, out, err);
        }
        return runFile(runner, args[0], out, err);
    }

    private static int runInteractive(CommandRunner runner, InputStream stdin, PrintStream out, PrintStream err) {
        try {
            runner.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandRunner runner, String fileArg, PrintStream out, PrintStream err) {
        String prefix = "ERROR: cannot read command file '" + fileArg + "': ";
        Path file;
        try {
            file = Path.of(fileArg);
        } catch (InvalidPathException e) {
            err.println(prefix + "invalid path");
            return 1;
        }
        if (!Files.exists(file)) {
            err.println(prefix + "file does not exist");
            return 1;
        }
        if (!Files.isRegularFile(file)) {
            err.println(prefix + "not a regular file");
            return 1;
        }
        if (!Files.isReadable(file)) {
            err.println(prefix + "file is not readable");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            runner.run(reader, out);
            return 0;
        } catch (IOException | UncheckedIOException e) {
            err.println(prefix + e.getMessage());
            return 1;
        }
    }
}
