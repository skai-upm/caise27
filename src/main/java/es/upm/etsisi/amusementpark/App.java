package es.upm.etsisi.amusementpark;

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
 * Entry point. Without arguments, commands are read from standard input
 * (interactive mode); with an argument, the first one is the path of a command
 * file (file mode). Both modes share the same {@link CommandRunner}.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application.
     *
     * @return 0 on normal termination, 1 if the users or the command file could not be read
     */
    static int run(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        CommandRunner runner;
        try {
            runner = new CommandRunner(new CommandProcessor(new UserManager(new UserFileRepository(usersFile))));
        } catch (IOException e) {
            out.println("ERROR: Could not load users from '" + usersFile + "': " + e);
            return 1;
        }
        if (args.length == 0) {
            BufferedReader in = new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8));
            return runner.run(in, out) ? 0 : 1;
        }
        String file = args[0];
        try (BufferedReader in = Files.newBufferedReader(Path.of(file), StandardCharsets.UTF_8)) {
            return runner.run(in, out) ? 0 : 1;
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR: Cannot open command file '" + file + "': " + e);
            return 1;
        }
    }
}
