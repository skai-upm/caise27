package es.upm.park.presentation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Shared command loop used by both interactive and file input: reads one command
 * per line, skips blank lines and stops on EXIT or end of input.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    public void run(BufferedReader in, PrintStream out) throws IOException {
        String line;
        while (!processor.isExitRequested() && (line = in.readLine()) != null) {
            if (!line.isBlank()) {
                out.println(processor.process(line));
            }
        }
    }
}
