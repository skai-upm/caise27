package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.cli.CommandProcessor;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.persistence.UserFileStorage;
import es.upm.amusementpark.persistence.UserStorage;
import es.upm.amusementpark.repository.UserRepository;
import es.upm.amusementpark.service.UserService;

/**
 * Application entry point.
 *
 * <p>Without arguments the application runs in interactive mode and reads commands from standard
 * input. With one argument it processes the commands contained in the given text file.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    private final PrintStream out;
    private final CommandProcessor processor;

    App(PrintStream out, PrintStream err, Path usersFile) throws IOException {
        this.out = out;
        UserStorage storage = new UserFileStorage(usersFile);
        UserRepository repository = new UserRepository();
        List<String> warnings = new ArrayList<>();
        for (User user : storage.load(warnings)) {
            repository.save(user);
        }
        warnings.forEach(err::println);
        this.processor = new CommandProcessor(new UserService(repository, storage));
    }

    public static void main(String[] args) {
        System.exit(execute(args, new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                System.out, System.err, USERS_FILE));
    }

    /**
     * Runs the application and returns the process exit status.
     */
    static int execute(String[] args, BufferedReader stdin, PrintStream out, PrintStream err, Path usersFile) {
        App app;
        try {
            app = new App(out, err, usersFile);
        } catch (IOException e) {
            err.println("Error: cannot load users from '" + usersFile + "'");
            return 1;
        }
        if (args.length == 0) {
            return app.runQuietly(stdin, err);
        }
        Path file = Path.of(args[0]);
        if (!Files.isRegularFile(file) || !Files.isReadable(file)) {
            err.println("Error: cannot read file '" + args[0] + "'");
            return 1;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return app.runQuietly(reader, err);
        } catch (IOException e) {
            err.println("Error: cannot read file '" + args[0] + "'");
            return 1;
        }
    }

    private int runQuietly(BufferedReader reader, PrintStream err) {
        try {
            run(reader);
            return 0;
        } catch (IOException e) {
            err.println("Error: " + e.getMessage());
            return 1;
        }
    }

    /**
     * Processes every line of the reader in order until the end of input.
     */
    public void run(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            processLine(line);
        }
    }

    /**
     * Handles a single command line and prints its response.
     */
    void processLine(String line) {
        String response = processor.process(line);
        if (response != null) {
            out.println(response);
        }
    }
}
