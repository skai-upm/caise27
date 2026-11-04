package es.upm.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Application entry point: reads commands from the terminal and prints their results.
 */
public class App {

    static final String DEFAULT_USERS_FILE = "users.txt";

    public static void main(String[] args) throws IOException {
        Path usersFile = Path.of(args.length > 0 ? args[0] : DEFAULT_USERS_FILE);
        run(usersFile, System.in, System.out);
    }

    static void run(Path usersFile, InputStream in, PrintStream out) throws IOException {
        CommandProcessor processor = new CommandProcessor(new UserManager(new UserFileStorage(usersFile)));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            out.println(processor.process(line));
        }
    }
}
