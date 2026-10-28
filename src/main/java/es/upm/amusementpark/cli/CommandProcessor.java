package es.upm.amusementpark.cli;

import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.service.UserOperationException;
import es.upm.amusementpark.service.UserService;

/**
 * Interprets text commands whose elements are separated by {@code ;} and returns the response to
 * print.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>  (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "HELP",
            "EXIT");

    private final UserService userService;
    private boolean exitRequested;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes one command line.
     *
     * @return the response to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            return switch (parts[0]) {
                case "USER CREATE" -> userCreate(parts);
                case "USER DELETE" -> userDelete(parts);
                case "USER UPDATE" -> userUpdate(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> error("Unknown command");
            };
        } catch (UserOperationException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected error processing command");
        }
    }

    /**
     * Tells whether an {@code EXIT} command has been processed; no further commands must run.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String help(String[] parts) throws UserOperationException {
        requireArguments(parts, 0, "HELP");
        return HELP_TEXT;
    }

    private String exit(String[] parts) throws UserOperationException {
        requireArguments(parts, 0, "EXIT");
        exitRequested = true;
        return "Goodbye";
    }

    private String userCreate(String[] parts) throws UserOperationException {
        requireArguments(parts, 7, "USER CREATE");
        userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "USER CREATED";
    }

    private String userDelete(String[] parts) throws UserOperationException {
        requireArguments(parts, 2, "USER DELETE");
        userService.deleteUser(parts[1], parts[2]);
        return "USER DELETED";
    }

    private String userUpdate(String[] parts) throws UserOperationException {
        requireArguments(parts, 4, "USER UPDATE");
        UserField field = UserField.fromName(parts[3])
                .orElseThrow(() -> new UserOperationException("Field cannot be modified: " + parts[3]));
        userService.updateUser(parts[1], parts[2], field, parts[4]);
        return "USER UPDATED";
    }

    private static void requireArguments(String[] parts, int expected, String command)
            throws UserOperationException {
        if (parts.length - 1 != expected) {
            throw new UserOperationException("Wrong number of arguments for " + command);
        }
    }

    private static String error(String reason) {
        return "ERROR: " + reason;
    }
}
