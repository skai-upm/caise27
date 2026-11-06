package es.upm.park.command;

import java.util.Arrays;
import java.util.stream.Collectors;

import es.upm.park.model.UserField;
import es.upm.park.persistence.PersistenceException;
import es.upm.park.service.UserException;
import es.upm.park.service.UserService;

/**
 * Interprets one text command (fields separated by ';') and returns the
 * message to show. Never throws for incorrect input.
 */
public class CommandProcessor {

    static final String USER_CREATE = "USER CREATE";
    static final String USER_DELETE = "USER DELETE";
    static final String USER_UPDATE = "USER UPDATE";
    static final String HELP = "HELP";
    static final String EXIT = "EXIT";
    static final String BYE = "Bye";
    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "  USER DELETE;<email>;<password>",
            "  USER UPDATE;<email>;<password>;<field>;<value>",
            "    <field>: " + Arrays.stream(UserField.values()).map(Enum::name).collect(Collectors.joining(", ")),
            "  HELP",
            "  EXIT");
    private static final int USER_CREATE_FIELDS = 7;
    private static final int USER_UPDATE_FIELDS = 4;
    private static final int USER_DELETE_FIELDS = 2;

    private final UserService userService;
    private boolean exitRequested;

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
            if (USER_DELETE.equals(name)) {
                return deleteUser(parts);
            }
            if (USER_UPDATE.equals(name)) {
                return updateUser(parts);
            }
            if (HELP.equals(name)) {
                return noFields(parts, HELP) ? HELP_TEXT : error(HELP + " expects 0 fields");
            }
            if (EXIT.equals(name)) {
                return exit(parts);
            }
            return error("unknown command '" + name + "'");
        } catch (UserException | PersistenceException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("command could not be processed");
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != USER_CREATE_FIELDS + 1) {
            return error(USER_CREATE + " expects " + USER_CREATE_FIELDS + " fields");
        }
        userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "User created: " + parts[3];
    }

    private String deleteUser(String[] parts) {
        if (parts.length != USER_DELETE_FIELDS + 1) {
            return error(USER_DELETE + " expects " + USER_DELETE_FIELDS + " fields");
        }
        userService.delete(parts[1], parts[2]);
        return "User deleted: " + parts[1];
    }

    private String updateUser(String[] parts) {
        if (parts.length != USER_UPDATE_FIELDS + 1) {
            return error(USER_UPDATE + " expects " + USER_UPDATE_FIELDS + " fields");
        }
        String dni = userService.update(parts[1], parts[2], parts[3], parts[4]).getDni();
        return "User updated: " + dni + " " + parts[3];
    }

    /**
     * Whether an EXIT command has been processed; no further command must
     * be processed afterwards.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String exit(String[] parts) {
        if (!noFields(parts, EXIT)) {
            return error(EXIT + " expects 0 fields");
        }
        exitRequested = true;
        return BYE;
    }

    private static boolean noFields(String[] parts, String command) {
        return parts.length == 1 && command.equals(parts[0].trim());
    }

    private static String error(String message) {
        return "Error: " + message;
    }
}
