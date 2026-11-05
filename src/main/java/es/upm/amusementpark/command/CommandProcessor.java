package es.upm.amusementpark.command;

import es.upm.amusementpark.service.UserOperationException;
import es.upm.amusementpark.service.UserService;

/**
 * Interprets text commands whose elements are separated by ';'.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String USER_CREATE = "USER CREATE";
    private static final int USER_CREATE_ELEMENTS = 8;

    private final UserService userService;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    /**
     * Processes one command line.
     *
     * @return the response to show, or an empty string when there is nothing to show
     */
    public String process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return "";
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            if (USER_CREATE.equals(parts[0])) {
                return userCreate(parts);
            }
            return "ERROR: unknown command '" + parts[0] + "'";
        } catch (UserOperationException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String userCreate(String[] p) {
        if (p.length != USER_CREATE_ELEMENTS) {
            return "ERROR: wrong number of arguments. Usage: USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card";
        }
        userService.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        return "USER CREATED";
    }
}
