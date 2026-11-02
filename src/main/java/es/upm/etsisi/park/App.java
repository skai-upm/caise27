package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard input;
 * with a path as first argument, commands are read from that file.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Path usersFile) {
        try {
            CommandProcessor processor = new CommandProcessor(new UserService(new UserRepository(usersFile)));
            if (args.length == 0) {
                processor.run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out);
            } else {
                processor.runFile(Path.of(args[0]), out);
            }
            return 0;
        } catch (UserException | IOException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
    }
}
