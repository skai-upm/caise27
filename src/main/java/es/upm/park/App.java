package es.upm.park;

import es.upm.park.cli.CommandProcessor;
import es.upm.park.cli.CommandRunner;
import es.upm.park.persistence.UserFileRepository;
import es.upm.park.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from
 * standard input; with one argument they are read from that file.
 */
public final class App {

    static final String USERS_FILE_PROPERTY = "park.users.file";
    static final String DEFAULT_USERS_FILE = "users.txt";

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        Path usersFile = Path.of(System.getProperty(USERS_FILE_PROPERTY, DEFAULT_USERS_FILE));
        UserService userService;
        try {
            userService = new UserService(new UserFileRepository(usersFile));
        } catch (UncheckedIOException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(userService), out);

        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            } catch (IOException e) {
                err.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
        }

        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: command file not found or not readable: " + commandFile);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
            return 0;
        } catch (IOException e) {
            err.println("ERROR: cannot read command file " + commandFile + ": " + e.getMessage());
            return 1;
        }
    }
}
