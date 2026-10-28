package es.upm.amusementpark.cli;

import es.upm.amusementpark.service.UserOperationException;
import es.upm.amusementpark.service.UserService;

/**
 * Interprets text commands whose elements are separated by {@code ;} and returns the response to
 * print.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private final UserService userService;

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
                default -> error("Unknown command");
            };
        } catch (UserOperationException e) {
            return error(e.getMessage());
        }
    }

    private String userCreate(String[] parts) throws UserOperationException {
        requireArguments(parts, 7, "USER CREATE");
        userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "USER CREATED";
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
