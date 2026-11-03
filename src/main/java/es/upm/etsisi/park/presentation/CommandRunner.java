package es.upm.etsisi.park.presentation;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Feeds command lines from any reader (standard input or a file) to the
 * shared {@link CommandProcessor}, in order, skipping blank lines.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    public void run(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String command = line.strip();
            if (command.isEmpty()) {
                continue;
            }
            if (!processor.process(command)) {
                return;
            }
        }
    }
}
