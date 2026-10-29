package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

/**
 * Processes text commands one line at a time. The same instance and logic are
 * used for every input source (standard input or a command file).
 */
public class CommandProcessor {

    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "HELP",
            "EXIT");

    private final PrintStream out;

    public CommandProcessor(PrintStream out) {
        this.out = out;
    }

    /**
     * Processes every line of the given source until the end of the input or
     * until an EXIT command is executed.
     */
    public void run(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            if (!execute(line)) {
                return;
            }
        }
    }

    /**
     * Executes a single command line.
     *
     * @return {@code false} when processing must stop (EXIT), {@code true} otherwise
     */
    public boolean execute(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        switch (command) {
            case "HELP":
                printHelp();
                return true;
            case "EXIT":
                out.println("Exiting application.");
                return false;
            default:
                out.println("Error: unknown command: " + command);
                return true;
        }
    }

    private void printHelp() {
        out.println("Available commands:");
        for (String syntax : COMMAND_SYNTAX) {
            out.println("  " + syntax);
        }
    }
}
