package es.upm.amusementpark;

import es.upm.amusementpark.command.CommandProcessor;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.persistence.UserFileStorage;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;
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
 * Amusement Park application entry point. Without arguments it reads commands from standard
 * input; with a file path as first argument it processes the commands of that file.
 */
public class App {

    private static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        int status = run(args, System.in, System.out, System.err, USERS_FILE);
        if (status != 0) {
            System.exit(status);
        }
    }

    /**
     * Runs the application.
     *
     * @return the process exit status
     */
    static int run(String[] args, InputStream in, PrintStream out, PrintStream err, Path usersFile) {
        CommandProcessor processor;
        try {
            Park park = Park.getInstance();
            park.start(new UserRepository(new UserFileStorage(usersFile)));
            processor = new CommandProcessor(new UserService(park.users()));
        } catch (UncheckedIOException e) {
            err.println("ERROR: cannot read users file '" + usersFile + "'");
            return 1;
        }
        if (args.length == 0) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            new InteractiveSession(stdin, out, processor).run();
            return 0;
        }
        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            err.println("ERROR: cannot read command file '" + args[0] + "'");
            return 1;
        }
        try (BufferedReader file = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            new InteractiveSession(file, out, processor).run();
            return 0;
        } catch (IOException | RuntimeException e) {
            err.println("ERROR: cannot read command file '" + args[0] + "'");
            return 1;
        }
    }
}
