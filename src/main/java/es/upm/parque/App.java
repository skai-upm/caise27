package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Entry point of the amusement-park application: reads commands from the terminal and prints the results.
 */
public class App {
    private static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        CommandProcessor processor = new CommandProcessor(new UserManager(new UserRepository(USERS_FILE)));
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.isBlank()) {
                    System.out.println(processor.process(line));
                }
            }
        } catch (IOException e) {
            System.out.println("ERROR: cannot read standard input: " + e.getMessage());
        }
    }
}
