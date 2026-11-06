package es.upm.park.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

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

    private CommandResult processSafely(String line) {
        try {
            return processor.process(line);
        } catch (RuntimeException e) {
            return CommandResult.of("ERROR: Could not process command: " + line.trim());
        }
    }
}
