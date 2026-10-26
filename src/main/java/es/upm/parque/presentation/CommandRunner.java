package es.upm.parque.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Executes commands read line by line from any source (terminal or file) until EXIT or end of input.
 */
public class CommandRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    public void run(BufferedReader reader) throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
