package es.upm.amusementpark.command;

import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.service.UserOperationException;
import es.upm.amusementpark.service.UserService;

import java.io.PrintStream;

/**
 * Interprets a single ';'-separated command line and executes it.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "  <field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
            "HELP",
            "EXIT");

    private final UserService userService;
    private final PrintStream out;

    public CommandProcessor(UserService userService, PrintStream out) {
        this.userService = userService;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} if the command was EXIT and processing must stop, {@code true} otherwise
     */
    public boolean process(String line) {
        String[] parts = line.trim().split(";", -1);
        try {
            switch (parts[0]) {
                case "USER CREATE" -> {
                    requireFields(parts, 8, "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>");
                    userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    out.println("USER CREATED");
                }
                case "USER DELETE" -> {
                    requireFields(parts, 3, "USER DELETE;<email>;<password>");
                    userService.delete(parts[1], parts[2]);
                    out.println("USER DELETED");
                }
                case "USER UPDATE" -> {
                    requireFields(parts, 5, "USER UPDATE;<email>;<password>;<field>;<value>");
                    UserField field = UserField.fromName(parts[3]);
                    if (field == null) {
                        throw new UserOperationException("field cannot be modified: " + parts[3]);
                    }
                    userService.update(parts[1], parts[2], field, parts[4]);
                    out.println("USER UPDATED");
                }
                case "HELP" -> {
                    requireFields(parts, 1, "HELP");
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireFields(parts, 1, "EXIT");
                    return false;
                }
                default -> out.println("ERROR: unknown command: " + parts[0]);
            }
        } catch (UserOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private static void requireFields(String[] parts, int expected, String syntax) throws UserOperationException {
        if (parts.length != expected) {
            throw new UserOperationException("wrong number of fields, expected " + syntax);
        }
    }
}
