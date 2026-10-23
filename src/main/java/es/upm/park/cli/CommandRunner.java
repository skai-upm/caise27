package es.upm.park.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Feeds lines from any reader (standard input or a command file) to the
 * command processor, printing each result. Blank lines are ignored and
 * processing stops as soon as EXIT is executed.
 */
public class CommandRunner {

    private final CommandProcessor processor;
    private final PrintStream out;

    public CommandRunner(CommandProcessor processor, PrintStream out) {
        this.processor = processor;
        this.out = out;
    }

    /**
     * Processes commands until EXIT or end of input.
     *
     * @return {@code true} if processing stopped because of EXIT
     */
    public boolean run(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue;
            }
            CommandResult result = processor.process(line);
            if (!result.output().isEmpty()) {
                out.println(result.output());
            }
            if (result.exit()) {
                return true;
            }
        }
        return false;
    }
}
