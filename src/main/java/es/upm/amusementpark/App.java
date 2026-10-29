package es.upm.amusementpark;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.command.CommandRunner;
import es.upm.amusementpark.persistence.UserRepository;
import es.upm.amusementpark.service.UserService;

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

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, UserRepository.DEFAULT_PATH);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application with the given streams and storage file.
     *
     * @return the process exit status
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path storage) {
        UserService userService;
        try {
            userService = new UserService(new UserRepository(storage, err));
        } catch (IOException e) {
            err.println("ERROR: could not load users: " + e.getMessage());
            return 1;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(userService, out));

        if (args.length == 0) {
            try {
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                err.println("ERROR: could not read standard input: " + e.getMessage());
                return 1;
            }
            return 0;
        }

        Path file = Path.of(args[0]);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            err.println("ERROR: command file not found or not readable: " + args[0]);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            err.println("ERROR: could not read command file: " + e.getMessage());
            return 1;
        }
        return 0;
    }
}
