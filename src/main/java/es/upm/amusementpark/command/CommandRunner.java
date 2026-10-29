package es.upm.amusementpark.command;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Feeds lines from any reader (standard input or a file) to the same {@link CommandProcessor}.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Processes lines in order, ignoring blank ones, until EXIT or end of input.
     */
    public void run(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            if (!processor.process(line)) {
                return;
            }
        }
    }
}
