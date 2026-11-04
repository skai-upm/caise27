package es.upm.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Terminal entry point: reads one command per line from standard input and prints the result.
 */
public class App {

    static final Path USERS_FILE = Path.of("users.txt");

    public static void main(String[] args) throws IOException {
        CommandProcessor processor = new CommandProcessor(new UserManager(new UserFileStore(USERS_FILE)));
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.isBlank()) {
                System.out.println(processor.process(line));
            }
        }
    }
}
