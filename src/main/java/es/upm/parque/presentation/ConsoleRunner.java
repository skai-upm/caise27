package es.upm.parque.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Feeds lines from a reader (standard input or a command file) to the command processor.
 */
public final class ConsoleRunner {

    private ConsoleRunner() {
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     * Read errors are reported and stop processing.
     *
     * @param echo whether each command is printed before its result (useful for file input)
     */
    public static void run(BufferedReader input, CommandProcessor processor, PrintStream out, boolean echo) {
        try {
            String line;
            while ((line = input.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (echo) {
                    out.println("> " + line);
                }
                CommandProcessor.Result result = processor.process(line);
                out.println(result.output());
                if (result.exit()) {
                    return;
                }
            }
        } catch (IOException e) {
            out.println("ERROR: cannot read commands: " + e.getMessage());
        }
    }
}
