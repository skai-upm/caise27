package es.upm.amusementpark;

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
 * Application entry point. Reads commands from standard input, or from the
 * file given as the first argument, and processes them one per line.
 */
public class App {

    static final Path DATA_DIR = Path.of("data");
    static final String USERS_FILE = "users.txt";

    public static void main(String[] args) {
        run(args, System.in, System.out, DATA_DIR);
    }

    static void run(String[] args, InputStream in, PrintStream out, Path dataDir) {
        UserRepository users;
        try {
            UserFileStorage storage = new UserFileStorage(dataDir.resolve(USERS_FILE));
            users = new UserRepository(storage);
            if (storage.getSkippedLines() > 0) {
                out.println("WARNING: " + storage.getSkippedLines() + " malformed line(s) in the users file were ignored.");
            }
        } catch (UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return;
        }
        CommandRunner runner = new CommandRunner(new CommandProcessor(out, new UserService(users)));
        if (args.length == 0) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                runner.run(reader);
            } catch (IOException e) {
                out.println("ERROR: could not read standard input: " + e.getMessage());
            }
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (IOException e) {
            out.println("ERROR: could not read command file: " + args[0]);
        }
    }
}
