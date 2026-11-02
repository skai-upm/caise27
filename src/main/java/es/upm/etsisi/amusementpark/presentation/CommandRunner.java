package es.upm.etsisi.amusementpark.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Reads command lines from any source (standard input or a file) and feeds them, in order,
 * to the same {@link CommandProcessor}. Blank lines are ignored; EXIT stops processing.
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
            CommandResult result = processor.process(line);
            out.println(result.output());
            if (result.exit()) {
                return;
            }
        }
    }
}
