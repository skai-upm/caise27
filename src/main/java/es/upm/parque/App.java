package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Entry point. Without arguments it reads commands from standard input;
 * with one argument it reads them from the given file.
 */
public class App {

    static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        run(args, USERS_FILE, System.in, System.out);
    }

    static void run(String[] args, Path usersFile, InputStream in, PrintStream out) {
        CommandProcessor processor;
        try {
            processor = new CommandProcessor(new UserManager(new UserRepository(usersFile)), out);
        } catch (RuntimeException e) {
            out.println("ERROR Cannot load stored data: " + e.getMessage());
            return;
        }
        if (args.length == 0) {
            try {
                processCommands(new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)), processor);
            } catch (IOException e) {
                out.println("ERROR Cannot read standard input: " + e.getMessage());
            }
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            processCommands(reader, processor);
        } catch (IOException | InvalidPathException e) {
            out.println("ERROR Cannot read command file '" + args[0] + "': " + describe(e));
        }
    }

    static void processCommands(BufferedReader reader, CommandProcessor processor) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            if (!processor.process(line)) {
                return;
            }
        }
    }

    private static String describe(Exception e) {
        if (e instanceof NoSuchFileException) {
            return "file not found";
        }
        if (e instanceof AccessDeniedException) {
            return "access denied";
        }
        return e.getClass().getSimpleName() + (e.getMessage() == null ? "" : " " + e.getMessage());
    }
}
