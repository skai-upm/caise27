package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Entry point of the amusement-park application. Without arguments commands are read from the terminal;
 * with one argument they are read from the given file. Both sources share the same processing.
 */
public class App {
    private static final Path USERS_FILE = Path.of("data", "users.txt");

    public static void main(String[] args) {
        run(args, System.in, System.out, USERS_FILE);
    }

    static void run(String[] args, InputStream stdin, PrintStream out, Path usersFile) {
        CommandProcessor processor = new CommandProcessor(new UserManager(new UserRepository(usersFile)),
                new SystemDate());
        if (args.length == 0) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8))) {
                processLines(in, processor, out);
            } catch (IOException e) {
                out.println("ERROR: cannot read standard input: " + e.getMessage());
            }
            return;
        }
        Path file = Path.of(args[0]);
        try (BufferedReader in = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            processLines(in, processor, out);
        } catch (NoSuchFileException e) {
            out.println("ERROR: command file not found: " + file);
        } catch (AccessDeniedException e) {
            out.println("ERROR: command file cannot be opened: " + file);
        } catch (IOException e) {
            out.println("ERROR: command file cannot be read: " + file + " (" + e.getMessage() + ")");
        }
    }

    private static void processLines(BufferedReader in, CommandProcessor processor, PrintStream out)
            throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = in.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
