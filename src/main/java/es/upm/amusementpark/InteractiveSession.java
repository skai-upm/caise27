package es.upm.amusementpark;

import es.upm.amusementpark.command.CommandProcessor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;

/**
 * Reads commands line by line from an input source until the end of input and prints their responses.
 */
public class InteractiveSession {

    private final BufferedReader input;
    private final PrintStream output;
    private final CommandProcessor processor;

    public InteractiveSession(BufferedReader input, PrintStream output, CommandProcessor processor) {
        this.input = input;
        this.output = output;
        this.processor = processor;
    }

    /**
     * Processes input lines until end of input or until EXIT is processed.
     *
     * @return number of lines read
     */
    public int run() {
        int lines = 0;
        try {
            String line;
            while (!processor.isExitRequested() && (line = input.readLine()) != null) {
                lines++;
                String response = processor.process(line);
                if (!response.isEmpty()) {
                    output.println(response);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return lines;
    }
}
