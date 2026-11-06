package es.upm.park.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Single command loop shared by interactive and file input: reads one command
 * per line, ignores blank lines, isolates failures of individual commands and
 * stops right after EXIT or at end of input.
 */
public class CommandRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            CommandResult result = processSafely(line);
            if (!result.output().isEmpty()) {
                out.println(result.output());
            }
            if (result.terminate()) {
                return;
            }
        }
    }

    /** Runs the commands of a text file; an unreadable file is reported and nothing runs. */
    public void runFile(String commandFile, PrintStream err) {
        try (BufferedReader file = Files.newBufferedReader(Path.of(commandFile), StandardCharsets.UTF_8)) {
            run(file);
        } catch (IOException e) {
            err.println("ERROR: Cannot read command file: " + commandFile);
        }
    }

    /** Runs commands typed interactively until EXIT or end of input. */
    public void runInteractive(BufferedReader input, PrintStream err) {
        try {
            run(input);
        } catch (IOException e) {
            err.println("ERROR: Cannot read input: " + e.getMessage());
        }
    }

    private CommandResult processSafely(String line) {
        try {
            return processor.process(line);
        } catch (RuntimeException e) {
            return CommandResult.of("ERROR: Could not process command: " + line.trim());
        }
    }
}
