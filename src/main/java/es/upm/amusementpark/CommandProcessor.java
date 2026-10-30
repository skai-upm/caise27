package es.upm.amusementpark;

import java.io.UncheckedIOException;

/**
 * Interprets one ';'-separated command line and executes it. Never throws for bad input: every
 * problem is reported as an "ERROR: ..." message.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>   (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "HELP",
            "EXIT");

    /**
     * Outcome of a command: the text to show and whether the application must stop.
     */
    public record Result(String output, boolean exit) {
    }

    private final UserService userService;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    public Result process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String command = parts[0];
        try {
            switch (command) {
                case "USER CREATE":
                    requireArguments(parts, 7, "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>");
                    userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    return new Result("USER CREATED", false);
                case "USER DELETE":
                    requireArguments(parts, 2, "USER DELETE;<email>;<password>");
                    userService.delete(parts[1], parts[2]);
                    return new Result("USER DELETED", false);
                case "USER UPDATE":
                    requireArguments(parts, 4, "USER UPDATE;<email>;<password>;<field>;<value>");
                    UserField field = UserField.fromName(parts[3]);
                    if (field == null) {
                        throw new IllegalArgumentException("field " + parts[3]
                                + " cannot be modified (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                    }
                    userService.update(parts[1], parts[2], field, parts[4]);
                    return new Result("USER UPDATED", false);
                case "HELP":
                    requireArguments(parts, 0, "HELP");
                    return new Result(HELP_TEXT, false);
                case "EXIT":
                    requireArguments(parts, 0, "EXIT");
                    return new Result("BYE", true);
                default:
                    return error("unknown command '" + command + "'. Type HELP to list the available commands");
            }
        } catch (IllegalArgumentException | UncheckedIOException e) {
            return error(e.getMessage());
        }
    }

    private static void requireArguments(String[] parts, int expected, String syntax) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("wrong number of arguments. Usage: " + syntax);
        }
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                throw new IllegalArgumentException("empty value in argument " + i + ". Usage: " + syntax);
            }
        }
    }

    private static Result error(String message) {
        return new Result("ERROR: " + message, false);
    }
}
