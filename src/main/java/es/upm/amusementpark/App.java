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
 * Application entry point. Without arguments commands are read from standard
 * input; with an argument, the first one is the path of a command file.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        run(args, System.in, System.out, USERS_FILE);
    }

    static void run(String[] args, InputStream in, PrintStream out, Path usersFile) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserStorage(usersFile)), out);
        } catch (IOException e) {
            out.println("Error: could not load users from " + usersFile + ": " + e.getMessage());
            return;
        }

        if (args.length == 0) {
            try {
                processor.run(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            } catch (IOException e) {
                out.println("Error: could not read standard input: " + e.getMessage());
            }
            return;
        }

        Path commandFile = Path.of(args[0]);
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("Error: command file not found or not readable: " + commandFile);
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            processor.run(reader);
        } catch (IOException e) {
            out.println("Error: could not read command file " + commandFile + ": " + e.getMessage());
        }
    }
}
