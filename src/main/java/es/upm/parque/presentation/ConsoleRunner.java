package es.upm.parque.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;

/**
 * Feeds the lines of a reader (standard input or a command file) to the command processor.
 */
public class ConsoleRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public ConsoleRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    /**
     * Processes lines until EXIT or the end of input and returns the exit code.
     */
    public int run(BufferedReader reader, String source) {
        try {
            String line;
            while (!processor.isFinished() && (line = reader.readLine()) != null) {
                String result = processor.process(line);
                if (result != null) {
                    out.println(result);
                }
            }
            return 0;
        } catch (IOException | UncheckedIOException e) {
            out.println("ERROR Cannot read commands from " + source);
            return 1;
        }
    }
}
