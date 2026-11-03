package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Entry point: starts the application and handles terminal interaction.
 * Reads one command per line until the end of the input and prints the response to each one.
 */
public class App {

    public static void main(String[] args) {
        run(System.in, System.out, Path.of(UserFileStorage.DEFAULT_FILE));
    }

    static void run(InputStream in, PrintStream out, Path usersFile) {
        CommandProcessor processor = new CommandProcessor(new UserService(new UserFileStorage(usersFile)));
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    out.println(processor.process(line));
                }
            }
        } catch (IOException e) {
            out.println("ERROR: Cannot read input: " + e.getMessage());
        }
    }
}
