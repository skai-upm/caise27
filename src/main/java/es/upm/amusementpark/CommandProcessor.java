package es.upm.amusementpark;

import java.io.PrintStream;
import java.io.UncheckedIOException;

/**
 * Processes single command lines. Every input source uses this class, so a
 * command behaves the same regardless of where it comes from. Command
 * elements are separated by ';'.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "  USER DELETE;<email>;<password>",
            "  USER UPDATE;<email>;<password>;<field>;<value>",
            "  HELP",
            "  EXIT");

    private static final String USER_CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";

    private final PrintStream out;
    private final UserService userService;

    public CommandProcessor(PrintStream out) {
        this(out, new UserService(new UserRepository()));
    }

    public CommandProcessor(PrintStream out, UserService userService) {
        this.out = out;
        this.userService = userService;
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
        String[] parts = command.split(";", -1);
        switch (parts[0]) {
            case "HELP":
                out.println(HELP_TEXT);
                return true;
            case "EXIT":
                out.println("Exiting application.");
                return false;
            case "USER CREATE":
                createUser(parts);
                return true;
            default:
                out.println("ERROR: unrecognised command: " + command);
                return true;
        }
    }

    private void createUser(String[] parts) {
        if (parts.length != 8) {
            out.println("ERROR: invalid syntax. Expected " + USER_CREATE_SYNTAX);
            return;
        }
        try {
            userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
            out.println("USER CREATED");
        } catch (UserOperationException | UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }
}
