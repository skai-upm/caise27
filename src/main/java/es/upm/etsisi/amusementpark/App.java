package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.command.CommandProcessor;
import es.upm.etsisi.amusementpark.command.CommandRunner;
import es.upm.etsisi.amusementpark.persistence.UserFileRepository;
import es.upm.etsisi.amusementpark.user.UserService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point: interactive mode without arguments, file mode with a command file path.
 */
public final class App {

    static final Path USERS_FILE = Path.of("users.txt");

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path usersFile) {
        CommandRunner runner;
        try {
            UserService users = new UserService(new UserFileRepository(usersFile, err));
            runner = new CommandRunner(new CommandProcessor(users), out);
        } catch (IOException e) {
            err.println("ERROR: could not load users from " + usersFile + ": " + e.getMessage());
            return 1;
        }
        if (args.length == 0) {
            return runInteractive(runner, in, err);
        }
        return runFile(runner, Path.of(args[0]), err);
    }

    private static int runInteractive(CommandRunner runner, InputStream in, PrintStream err) {
        try {
            runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            return 0;
        } catch (IOException e) {
            err.println("ERROR: could not read standard input: " + e.getMessage());
            return 1;
        }
    }

    private static int runFile(CommandRunner runner, Path commands, PrintStream err) {
        if (!Files.isRegularFile(commands) || !Files.isReadable(commands)) {
            err.println("ERROR: command file not found or not readable: " + commands);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commands, StandardCharsets.UTF_8)) {
            runner.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: could not read command file " + commands + ": " + e.getMessage());
            return 1;
        }
    }
}
