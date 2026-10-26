package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Starts the application. Without arguments commands are read from the terminal;
 * otherwise the first argument is the path of a file with one command per line.
 */
public class App {

    private static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(USERS_FILE)), SystemDate.today());
        } catch (IOException e) {
            System.out.println("ERROR Could not load users: " + e.getMessage());
            return;
        }
        CommandRunner runner = new CommandRunner(processor, System.out);
        if (args.length == 0) {
            runInteractive(runner);
        } else {
            runFile(runner, args[0]);
        }
    }

    private static void runInteractive(CommandRunner runner) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            runner.run(reader);
        } catch (IOException e) {
            System.out.println("ERROR Could not read input: " + e.getMessage());
        }
    }

    private static void runFile(CommandRunner runner, String path) {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            runner.run(reader);
        } catch (NoSuchFileException e) {
            System.out.println("ERROR Cannot read command file '" + path + "': file does not exist");
        } catch (IOException | InvalidPathException e) {
            System.out.println("ERROR Cannot read command file '" + path + "': " + e.getMessage());
        }
    }
}
