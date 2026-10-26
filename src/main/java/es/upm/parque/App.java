package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Starts the application and reads commands from the terminal, one per line.
 */
public class App {

    private static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(USERS_FILE)));
        } catch (IOException e) {
            System.out.println("ERROR Could not load users: " + e.getMessage());
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    System.out.println(processor.process(line));
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR Could not read input: " + e.getMessage());
        }
    }
}
