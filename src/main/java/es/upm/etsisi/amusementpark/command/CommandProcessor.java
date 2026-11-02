package es.upm.etsisi.amusementpark.command;

import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserField;
import es.upm.etsisi.amusementpark.user.UserManager;
import es.upm.etsisi.amusementpark.user.UserOperationException;

/**
 * Interprets and executes one command line. Elements are separated by ';' and
 * parsed with String.split; the same processor serves interactive and file input.
 */
public class CommandProcessor {
    static final String USER_CREATE = "USER CREATE";
    static final String USER_DELETE = "USER DELETE";
    static final String USER_UPDATE = "USER UPDATE";
    static final String HELP = "HELP";
    static final String EXIT = "EXIT";

    static final String USER_CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    static final String USER_DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  " + USER_CREATE_SYNTAX,
            "  " + USER_DELETE_SYNTAX,
            "  " + USER_UPDATE_SYNTAX + "   (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  " + HELP,
            "  " + EXIT);

    private final UserManager users;

    public CommandProcessor(UserManager users) {
        this.users = users;
    }

    public CommandResult process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String keyword = parts[0];
        try {
            return switch (keyword) {
                case USER_CREATE -> createUser(parts);
                case USER_DELETE -> deleteUser(parts);
                case USER_UPDATE -> updateUser(parts);
                case HELP -> parts.length == 1 ? CommandResult.message(HELP_TEXT) : usage(HELP);
                case EXIT -> parts.length == 1 ? new CommandResult("BYE", true) : usage(EXIT);
                default -> CommandResult.error("unknown command '" + keyword + "'");
            };
        } catch (UserOperationException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    private CommandResult createUser(String[] p) throws UserOperationException {
        if (p.length != 8) {
            return usage(USER_CREATE_SYNTAX);
        }
        users.create(new User(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        return CommandResult.message("USER CREATED");
    }

    private CommandResult deleteUser(String[] p) throws UserOperationException {
        if (p.length != 3) {
            return usage(USER_DELETE_SYNTAX);
        }
        users.delete(p[1], p[2]);
        return CommandResult.message("USER DELETED");
    }

    private CommandResult updateUser(String[] p) throws UserOperationException {
        if (p.length != 5) {
            return usage(USER_UPDATE_SYNTAX);
        }
        UserField field = UserField.fromName(p[3]);
        if (field == null) {
            return CommandResult.error("field '" + p[3] + "' cannot be modified; allowed: NAME, SURNAME, EMAIL, "
                    + "PASSWORD, PHONE, CARD");
        }
        users.update(p[1], p[2], field, p[4]);
        return CommandResult.message("USER UPDATED");
    }

    private static CommandResult usage(String syntax) {
        return CommandResult.error("wrong number of elements; usage: " + syntax);
    }
}
