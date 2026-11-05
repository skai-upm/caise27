package es.upm.amusementpark.command;

import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.service.UserOperationException;
import es.upm.amusementpark.service.UserService;

/**
 * Interprets text commands whose elements are separated by ';'.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String USER_CREATE = "USER CREATE";
    private static final int USER_CREATE_ELEMENTS = 8;
    private static final String USER_DELETE = "USER DELETE";
    private static final int USER_DELETE_ELEMENTS = 3;
    private static final String USER_UPDATE = "USER UPDATE";
    private static final int USER_UPDATE_ELEMENTS = 5;
    private static final String HELP = "HELP";
    private static final String EXIT = "EXIT";
    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "  USER DELETE;<email>;<password>",
            "  USER UPDATE;<email>;<password>;<field>;<value>   (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  HELP",
            "  EXIT");

    private boolean exitRequested;

    private final UserService userService;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    /** Whether an EXIT command has been processed; no further commands must run. */
    public boolean isExitRequested() {
        return exitRequested;
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
            if (HELP.equals(parts[0])) {
                return parts.length == 1 ? HELP_TEXT : "ERROR: wrong number of arguments. Usage: HELP";
            }
            if (EXIT.equals(parts[0])) {
                if (parts.length != 1) {
                    return "ERROR: wrong number of arguments. Usage: EXIT";
                }
                exitRequested = true;
                return "GOODBYE";
            }
            if (USER_CREATE.equals(parts[0])) {
                return userCreate(parts);
            }
            if (USER_DELETE.equals(parts[0])) {
                return userDelete(parts);
            }
            if (USER_UPDATE.equals(parts[0])) {
                return userUpdate(parts);
            }
            return "ERROR: unknown command '" + parts[0] + "'";
        } catch (UserOperationException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: the command could not be processed (" + e.getMessage() + ")";
        }
    }

    private String userCreate(String[] p) {
        if (p.length != USER_CREATE_ELEMENTS) {
            return "ERROR: wrong number of arguments. Usage: USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card";
        }
        userService.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        return "USER CREATED";
    }

    private String userDelete(String[] p) {
        if (p.length != USER_DELETE_ELEMENTS) {
            return "ERROR: wrong number of arguments. Usage: USER DELETE;Email;Password";
        }
        userService.deleteUser(p[1], p[2]);
        return "USER DELETED";
    }

    private String userUpdate(String[] p) {
        if (p.length != USER_UPDATE_ELEMENTS) {
            return "ERROR: wrong number of arguments. Usage: USER UPDATE;Email;Password;Field;NewValue";
        }
        if ("DNI".equals(p[3])) {
            return "ERROR: the DNI cannot be modified";
        }
        UserField field = UserField.fromName(p[3]).orElse(null);
        if (field == null) {
            return "ERROR: unknown field '" + p[3] + "'. Modifiable fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD";
        }
        userService.updateUser(p[1], p[2], field, p[4]);
        return "USER UPDATED";
    }
}
