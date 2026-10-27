package es.upm.amusementpark;

import java.io.PrintStream;

/**
 * Processes single command lines. Every input source uses this class, so a
 * command behaves the same regardless of where it comes from.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "  USER DELETE;<email>;<password>",
            "  USER UPDATE;<email>;<password>;<field>;<value>",
            "  HELP",
            "  EXIT");

    private final PrintStream out;

    public CommandProcessor(PrintStream out) {
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} when the application must stop processing commands
     */
    public boolean process(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        switch (command) {
            case "HELP":
                out.println(HELP_TEXT);
                return true;
            case "EXIT":
                out.println("Exiting application.");
                return false;
            default:
                out.println("Error: unrecognised command: " + command);
                return true;
        }
    }
}
