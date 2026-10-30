package es.upm.etsisi.amusementpark;

import es.upm.etsisi.amusementpark.cli.CommandProcessor;
import es.upm.etsisi.amusementpark.cli.CommandRunner;
import es.upm.etsisi.amusementpark.controller.UserController;
import es.upm.etsisi.amusementpark.repository.UserFileStore;
import es.upm.etsisi.amusementpark.repository.UserRepository;

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
 * Application entry point: interactive mode without arguments, file mode with a command file path.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        System.exit(run(args, USERS_FILE, System.in, System.out));
    }

    static int run(String[] args, Path usersFile, InputStream in, PrintStream out) {
        try {
            UserRepository repository = new UserRepository(new UserFileStore(usersFile));
            CommandRunner runner = new CommandRunner(
                    new CommandProcessor(new UserController(repository), out));

            if (args.length == 0) {
                out.println("Amusement Park - type HELP for the list of commands");
                runner.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
                return 0;
            }

            Path commandFile = Path.of(args[0]);
            if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
                out.println("ERROR: Command file not found or not readable: " + commandFile);
                return 1;
            }
            try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
                runner.run(reader);
            }
            return 0;
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return 1;
        }
    }
}
