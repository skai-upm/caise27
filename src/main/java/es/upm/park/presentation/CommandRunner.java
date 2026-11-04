package es.upm.park.presentation;

import es.upm.park.business.model.Result;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Feeds commands from any line source to the {@link CommandProcessor}. Used identically for
 * standard input and command files: blank lines are skipped and processing stops after a
 * terminating command (EXIT) or at end of input.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    public void run(BufferedReader in, PrintStream out) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            Result result = processor.process(line);
            out.println(result);
            if (result.terminate()) {
                return;
            }
        }
    }
}
