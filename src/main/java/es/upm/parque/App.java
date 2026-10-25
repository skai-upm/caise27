package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

/**
 * Starts the application. Without arguments commands are read from standard input;
 * with an argument they are read from the file it names. Both modes share the same processing.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        start(args, System.in, System.out, USERS_FILE);
    }

    static void start(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(usersFile)));
        } catch (IOException e) {
            out.println("ERROR " + e.getMessage());
            return;
        }
        if (args.length == 0) {
            try {
                run(new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8)), out, processor);
            } catch (IOException e) {
                out.println("ERROR CANNOT READ STANDARD INPUT");
            }
            return;
        }
        String path = args[0];
        try (BufferedReader file = Files.newBufferedReader(Path.of(path), StandardCharsets.UTF_8)) {
            run(file, out, processor);
        } catch (IOException | UncheckedIOException | InvalidPathException e) {
            out.println("ERROR CANNOT READ FILE " + path);
        }
    }

    static void run(BufferedReader in, PrintStream out, CommandProcessor processor) throws IOException {
        String line;
        while (!processor.isFinished() && (line = in.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
