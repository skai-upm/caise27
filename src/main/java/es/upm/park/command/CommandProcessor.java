package es.upm.park.command;

import es.upm.park.persistence.PersistenceException;
import es.upm.park.service.UserException;
import es.upm.park.service.UserService;

/**
 * Interprets one text command (fields separated by ';') and returns the
 * message to show. Never throws for incorrect input.
 */
public class CommandProcessor {

    static final String USER_CREATE = "USER CREATE";
    private static final int USER_CREATE_FIELDS = 7;

    private final UserService userService;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes a command line.
     *
     * @return the message to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        String name = parts[0].trim();
        try {
            if (USER_CREATE.equals(name)) {
                return createUser(parts);
            }
            return error("unknown command '" + name + "'");
        } catch (UserException | PersistenceException e) {
            return error(e.getMessage());
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != USER_CREATE_FIELDS + 1) {
            return error(USER_CREATE + " expects " + USER_CREATE_FIELDS + " fields");
        }
        userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "User created: " + parts[3];
    }

    private static String error(String message) {
        return "Error: " + message;
    }
}
