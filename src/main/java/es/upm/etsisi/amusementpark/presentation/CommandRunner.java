package es.upm.etsisi.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Command-based interaction: reads one command per line from standard input or from a command file and
 * prints the response of the {@link CommandProcessor} to each one. Both sources share the same loop.
 */
public final class CommandRunner {

    private CommandRunner() {
    }

    /** Runs the commands typed on standard input. */
    public static void runStandardInput(BufferedReader in, PrintStream out, CommandProcessor processor) {
        try {
            run(in, out, processor);
        } catch (IOException e) {
            out.println("ERROR: Cannot read standard input: " + e.getMessage());
        }
    }

    /** Runs the commands of a file, reporting a missing or unreadable file without an exception. */
    public static void runFile(Path commandFile, PrintStream out, CommandProcessor processor) {
        if (!Files.isRegularFile(commandFile) || !Files.isReadable(commandFile)) {
            out.println("ERROR: Cannot read command file " + commandFile + ": it does not exist or is not a readable file");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            run(reader, out, processor);
        } catch (IOException e) {
            out.println("ERROR: Cannot read command file " + commandFile + ": " + e.getMessage());
        }
    }

    /** Processes each non-blank line in order and stops after EXIT or at the end of the input. */
    static void run(BufferedReader reader, PrintStream out, CommandProcessor processor) throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
