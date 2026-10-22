package es.upm.etsisi.park.presentation;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Feeds command lines from any reader to a {@link CommandProcessor}. Interactive and
 * file mode only differ in the reader they pass in.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Processes lines until EXIT or end of input. Blank lines are ignored.
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
