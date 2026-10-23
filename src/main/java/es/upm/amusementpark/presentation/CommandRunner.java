package es.upm.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Reads commands line by line and executes them with a {@link CommandProcessor}.
 * The same runner serves standard input and command files, so both modes
 * behave identically.
 */
public class CommandRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    /**
     * Processes lines until EXIT is executed or the input ends. Blank lines are
     * skipped.
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            CommandResult result = execute(line);
            if (!result.output().isEmpty()) {
                out.println(result.output());
            }
            if (result.exit()) {
                return;
            }
        }
    }

    private CommandResult execute(String line) {
        try {
            return processor.process(line);
        } catch (RuntimeException e) {
            return CommandResult.error("Unexpected error while processing the command: " + e.getMessage());
        }
    }
}
