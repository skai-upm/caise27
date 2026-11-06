package es.upm.park.command;

/**
 * Interprets one command line, independently of where the line comes from.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "HELP",
            "EXIT");

    static final String EXIT_TEXT = "Exiting the application.";

    public CommandResult process(String line) {
        String command = line == null ? "" : line.trim();
        switch (command) {
            case "HELP":
                return CommandResult.of(HELP_TEXT);
            case "EXIT":
                return CommandResult.exit(EXIT_TEXT);
            default:
                return CommandResult.of("ERROR: Unknown command: " + command);
        }
    }
}
