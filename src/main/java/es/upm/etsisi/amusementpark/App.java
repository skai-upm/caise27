package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Entry point: reads text commands from the terminal (standard input), one per
 * line, and prints one result line for each.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserFileRepository(USERS_FILE)));
        } catch (IOException e) {
            System.err.println("ERROR: could not load users from " + USERS_FILE + ": " + e.getMessage());
            return;
        }
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                String result = processor.process(line);
                if (result != null) {
                    System.out.println(result);
                }
            }
        } catch (IOException e) {
            System.err.println("ERROR: could not read from the terminal: " + e.getMessage());
        }
    }
}
