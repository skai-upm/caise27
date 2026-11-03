package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments it reads commands from standard
 * input; with one argument it reads them from that file.
 */
public class App {

    static final String USERS_FILE_PROPERTY = "park.users.file";
    static final String DEFAULT_USERS_FILE = "users.txt";

    public static void main(String[] args) {
        int status = run(args, System.in, System.out);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application and returns its exit status.
     */
    static int run(String[] args, InputStream in, PrintStream out) {
        Path usersFile = Path.of(System.getProperty(USERS_FILE_PROPERTY, DEFAULT_USERS_FILE));
        UserService users;
        try {
            users = new UserService(new UserRepository(usersFile));
        } catch (IOException e) {
            out.println("ERROR: cannot load users from " + usersFile + ": " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(users, out));

        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        Path commandFile = Path.of(args[0]);
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            out.println("ERROR: cannot read file " + commandFile);
            return 1;
        }
        return 0;
    }
}
