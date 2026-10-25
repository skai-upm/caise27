package es.upm.parque.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Feeds command lines from a reader (standard input or a command file) to the command processor.
 */
public class InputRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public InputRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    /**
     * Processes non-blank lines until EXIT or the end of the input.
     */
    public void run(BufferedReader reader) {
        try {
            String line;
            while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                out.println(processor.process(line));
            }
        } catch (IOException e) {
            out.println("ERROR reading commands: " + e.getMessage());
        }
    }
}
