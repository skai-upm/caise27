package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Entry point: starts the application and handles terminal interaction.
 * Without arguments commands are read from standard input; with an argument, the first one is the
 * path of a text file with one command per line. Both sources share the same processing loop.
 * Data files ({@code users.txt}, {@code groups.txt}) are kept in the working directory.
 */
public class App {

    public static void main(String[] args) {
        Path dataDir = Path.of(".");
        if (args.length == 0) {
            runStandardInput(new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)),
                    System.out, dataDir);
        } else {
            runFile(Path.of(args[0]), System.out, dataDir);
        }
    }

    /** Runs the commands typed on standard input. */
    static void runStandardInput(BufferedReader in, PrintStream out, Path dataDir) {
        try {
            run(in, out, dataDir);
        } catch (IOException e) {
            out.println("ERROR: Cannot read standard input: " + e.getMessage());
        }
    }

    /** Runs the commands of a file, reporting a missing or unreadable file without an exception. */
    static void runFile(Path commandFile, PrintStream out, Path dataDir) {
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("ERROR: Cannot read command file " + commandFile + ": it does not exist or is not a readable file");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            run(reader, out, dataDir);
        } catch (IOException e) {
            out.println("ERROR: Cannot read command file " + commandFile + ": " + e.getMessage());
        }
    }

    /** Builds the controller and its services over the data files kept in {@code dataDir}. */
    static CommandProcessor createProcessor(Path dataDir) {
        UserService users = new UserService(new UserFileStorage(dataDir.resolve(UserFileStorage.DEFAULT_FILE)));
        GroupService groups = new GroupService(new GroupFileStorage(dataDir.resolve(GroupFileStorage.DEFAULT_FILE)), users);
        return new CommandProcessor(users, groups);
    }

    /**
     * Shared command loop for both input modes: processes each non-blank line in order and stops
     * after EXIT or at the end of the input.
     */
    static void run(BufferedReader reader, PrintStream out, Path dataDir) throws IOException {
        CommandProcessor processor = createProcessor(dataDir);
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
