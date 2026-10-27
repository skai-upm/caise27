package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Feeds the lines of an input source to the {@link CommandProcessor} until
 * EXIT is processed or the input ends.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (!processor.process(line)) {
                return;
            }
        }
    }
}
