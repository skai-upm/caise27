package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments it reads commands from standard input;
 * with a file path as first argument it processes the commands in that file.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        System.exit(run(args, System.in, System.out, USERS_FILE));
    }

    static int run(String[] args, InputStream in, PrintStream out, Path usersFile) {
        UserService service;
        try {
            service = new UserService(new UserRepository(usersFile));
        } catch (RuntimeException e) {
            out.println("ERROR: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(service, out));
        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.println("ERROR: cannot read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }
        Path commands = Path.of(args[0]);
        if (!Files.isRegularFile(commands) || !Files.isReadable(commands)) {
            out.println("ERROR: command file not found or not readable: " + args[0]);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commands, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            out.println("ERROR: cannot read command file: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
