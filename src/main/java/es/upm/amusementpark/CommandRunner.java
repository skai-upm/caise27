package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Feeds lines from any source (standard input or a command file) to the command processor,
 * so both input modes share exactly the same processing.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

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
