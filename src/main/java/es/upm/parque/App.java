package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Entry point: reads commands from the terminal and prints one response per command.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");

    private final CommandProcessor processor;

    public App(CommandProcessor processor) {
        this.processor = processor;
    }

    public static void main(String[] args) {
        UserManager userManager = new UserManager(new UserRepository(USERS_FILE));
        App app = new App(new CommandProcessor(userManager));
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        app.run(in, System.out);
    }

    /**
     * Processes every non-blank line until the input ends.
     */
    public void run(BufferedReader in, PrintStream out) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (!line.isBlank()) {
                    out.println(processor.process(line));
                }
            }
        } catch (IOException e) {
            out.println("ERROR Cannot read input: " + e.getMessage());
        }
    }
}
