package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Starts the application and runs the terminal loop: one command per line until the input ends.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) throws IOException {
        CommandProcessor processor = new CommandProcessor(new UserManager(new UserStorage(USERS_FILE)));
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            String response = processor.execute(line);
            if (response != null) {
                System.out.println(response);
            }
        }
    }
}
