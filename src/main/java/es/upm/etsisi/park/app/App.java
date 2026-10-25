package es.upm.etsisi.park.app;

import es.upm.etsisi.park.command.CommandProcessor;
import es.upm.etsisi.park.persistence.PersistenceException;
import es.upm.etsisi.park.persistence.UserFileRepository;
import es.upm.etsisi.park.service.UserService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments reads commands from standard input;
 * with one argument reads them from the given file.
 */
public final class App {

    static final String USERS_FILE_PROPERTY = "park.users.file";
    static final String DEFAULT_USERS_FILE = "users.txt";

    private App() {
    }

    public static void main(String[] args) {
        int status = run(args, System.out, System.err);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        Path usersFile = Path.of(System.getProperty(USERS_FILE_PROPERTY, DEFAULT_USERS_FILE));
        UserService userService;
        try {
            userService = new UserService(new UserFileRepository(usersFile, err));
        } catch (PersistenceException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(userService), out);

        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)), true);
            } catch (IOException e) {
                err.println("ERROR: Cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: Command file '" + args[0] + "' does not exist or cannot be read");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader, false);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file '" + args[0] + "': " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
