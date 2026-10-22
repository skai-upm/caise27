package es.upm.etsisi.park;

import es.upm.etsisi.park.cli.CommandProcessor;
import es.upm.etsisi.park.cli.CommandRunner;
import es.upm.etsisi.park.persistence.UserFileStore;
import es.upm.etsisi.park.repository.UserRepository;
import es.upm.etsisi.park.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Application entry point. Without arguments commands are read from standard input;
 * with an argument, from the given command file.
 */
public class App {

    private static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        System.exit(run(args));
    }

    static int run(String[] args) {
        CommandRunner runner;
        try {
            UserService userService = new UserService(new UserRepository(new UserFileStore(USERS_FILE)));
            runner = new CommandRunner(new CommandProcessor(userService, System.out));
        } catch (UncheckedIOException e) {
            System.err.println("ERROR: " + e.getMessage());
            return 1;
        }

        if (args.length == 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
                runner.run(reader);
            } catch (IOException e) {
                System.err.println("ERROR: Cannot read standard input");
                return 1;
            }
            return 0;
        }

        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            System.err.println("ERROR: Command file not found or not readable: " + args[0]);
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            System.err.println("ERROR: Cannot read command file: " + args[0]);
            return 1;
        }
        return 0;
    }
}
