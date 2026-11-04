package es.upm.park;

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
 * Entry point. Without arguments commands are read from standard input; otherwise the first
 * argument is the path of a command file. Both sources share the same processing.
 */
public class App {

    /** Data files live in the working directory. */
    static final Path DATA_DIR = Path.of("");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    /** Runs the application and returns its exit status (0 = normal end). */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err) {
        return run(args, stdin, out, err, DATA_DIR);
    }

    /** Runs with {@code users.txt}, {@code groups.txt} and {@code tickets.txt} inside {@code dataDir}. */
    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path dataDir) {
        CommandRunner runner;
        try {
            UserManager users = new UserManager(new UserFileStore(dataDir.resolve("users.txt")));
            GroupManager groups = new GroupManager(new GroupFileStore(dataDir.resolve("groups.txt")), users);
            TicketManager tickets = new TicketManager(new TicketFileStore(dataDir.resolve("tickets.txt")), users);
            runner = new CommandRunner(new CommandProcessor(users, groups, tickets));
        } catch (IOException e) {
            err.println("ERROR: Cannot load data files: " + describe(e));
            return 1;
        }
        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            } catch (IOException e) {
                err.println("ERROR: Cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }
        String commandFile = args[0];
        Path path;
        try {
            path = Path.of(commandFile);
        } catch (InvalidPathException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        try (BufferedReader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            runner.run(in, out);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file '" + commandFile + "': " + describe(e));
            return 1;
        }
        return 0;
    }

    private static String describe(Exception e) {
        String reason = e.getMessage();
        return e.getClass().getSimpleName() + (reason == null ? "" : " (" + reason + ")");
    }
}
