package es.upm.etsisi.amusementpark.cli;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Feeds lines from any source (standard input or a file) to the command processor.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Processes lines in order, skipping blank ones, until EXIT or end of input.
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
