package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * The single read-process loop shared by interactive and file mode: reads one
 * command per line, prints its result, and stops on {@code EXIT} or end of input.
 */
public class CommandRunner {

    private final CommandProcessor processor;

    public CommandRunner(CommandProcessor processor) {
        this.processor = processor;
    }

    /**
     * Processes commands from {@code in} until EXIT or end of input.
     *
     * @return {@code true} if input was consumed without read errors
     */
    public boolean run(BufferedReader in, PrintStream out) {
        try {
            String line;
            while (!processor.isExitRequested() && (line = in.readLine()) != null) {
                String result = processor.process(line);
                if (result != null) {
                    out.println(result);
                }
            }
            return true;
        } catch (IOException e) {
            out.println("ERROR: Could not read commands: " + e);
            return false;
        }
    }
}
