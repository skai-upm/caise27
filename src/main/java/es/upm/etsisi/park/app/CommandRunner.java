package es.upm.etsisi.park.app;

import es.upm.etsisi.park.command.CommandProcessor;
import es.upm.etsisi.park.command.CommandResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Reads commands line by line from any source and hands them to the shared {@link CommandProcessor}.
 */
public class CommandRunner {

    private static final String PROMPT = "> ";

    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    /**
     * Processes commands until EXIT is executed or the input ends.
     *
     * @param interactive whether to print a prompt before each command
     */
    public void run(BufferedReader input, boolean interactive) throws IOException {
        while (true) {
            if (interactive) {
                out.print(PROMPT);
                out.flush();
            }
            String line = input.readLine();
            if (line == null) {
                return;
            }
            if (line.isBlank()) {
                continue;
            }
            CommandResult result = processor.process(line);
            if (!result.output().isEmpty()) {
                out.println(result.output());
            }
            if (result.exit()) {
                return;
            }
        }
    }
}
