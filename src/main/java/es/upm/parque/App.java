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
    private static final Path DATA_DIR = Path.of("data");

    public static void main(String[] args) {
        run(args, System.in, System.out, DATA_DIR);
    }

    static void run(String[] args, InputStream stdin, PrintStream out, Path dataDir) {
        UserManager userManager = new UserManager(new UserRepository(dataDir.resolve("users.txt")));
        GroupManager groupManager = new GroupManager(new GroupRepository(dataDir.resolve("groups.txt")), userManager);
        CommandProcessor processor = new CommandProcessor(userManager, groupManager, new SystemDate());
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
