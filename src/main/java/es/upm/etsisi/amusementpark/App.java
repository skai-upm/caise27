package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard
 * input; with one argument they are read from the named text file.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path usersFile) {
        UserService userService;
        try {
            userService = new UserService(new UserRepository(usersFile, err));
        } catch (UserException e) {
            err.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandProcessor processor = new CommandProcessor(userService, out);
        try {
            if (args.length == 0) {
                processor.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } else {
                Path commandFile = Path.of(args[0]);
                if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
                    err.println("ERROR: cannot read command file " + args[0]);
                    return 1;
                }
                processor.runFile(commandFile);
            }
        } catch (IOException e) {
            err.println("ERROR: failed reading commands: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
