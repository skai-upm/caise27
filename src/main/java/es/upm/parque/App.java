package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Starts the application and reads commands from the terminal, one per line.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        PrintStream out = System.out;
        try {
            CommandProcessor processor = new CommandProcessor(new UserManager(new UserRepository(USERS_FILE)));
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            run(in, out, processor);
        } catch (IOException e) {
            out.println("ERROR " + e.getMessage());
        }
    }

    static void run(BufferedReader in, PrintStream out, CommandProcessor processor) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
