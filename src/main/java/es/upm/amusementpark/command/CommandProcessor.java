package es.upm.amusementpark.command;

import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.service.UserException;
import es.upm.amusementpark.service.UserService;
import java.util.Locale;

/**
 * Interprets one {@code ;}-separated command line and executes it. Parsing
 * relies only on basic String operations and {@code split}.
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
            "  " + USER_UPDATE_SYNTAX + "   (field: " + UserField.allNames() + ")",
            "  " + HELP,
            "  " + EXIT);

    private final UserService userService;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    public CommandResult process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return CommandResult.of("");
        }
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String keyword = parts[0].toUpperCase(Locale.ROOT);
        try {
            switch (keyword) {
                case USER_CREATE:
                    return createUser(parts);
                case USER_DELETE:
                    return deleteUser(parts);
                case USER_UPDATE:
                    return updateUser(parts);
                case HELP:
                    return parts.length == 1 ? CommandResult.of(HELP_TEXT) : wrongArguments(HELP);
                case EXIT:
                    return parts.length == 1 ? new CommandResult("Bye", true) : wrongArguments(EXIT);
                default:
                    return CommandResult.error("Unknown command '" + parts[0] + "'. Type HELP to list commands");
            }
        } catch (UserException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    private CommandResult createUser(String[] parts) throws UserException {
        if (parts.length != 8) {
            return wrongArguments(USER_CREATE_SYNTAX);
        }
        userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return CommandResult.of("USER CREATED");
    }

    private CommandResult deleteUser(String[] parts) throws UserException {
        if (parts.length != 3) {
            return wrongArguments(USER_DELETE_SYNTAX);
        }
        userService.delete(parts[1], parts[2]);
        return CommandResult.of("USER DELETED");
    }

    private CommandResult updateUser(String[] parts) throws UserException {
        if (parts.length != 5) {
            return wrongArguments(USER_UPDATE_SYNTAX);
        }
        UserField field = UserField.fromString(parts[3]);
        if (field == null) {
            return CommandResult.error("Field '" + parts[3] + "' cannot be modified. Allowed fields: "
                    + UserField.allNames());
        }
        userService.update(parts[1], parts[2], field, parts[4]);
        return CommandResult.of("USER UPDATED");
    }

    private static CommandResult wrongArguments(String syntax) {
        return CommandResult.error("Wrong number of arguments. Usage: " + syntax);
    }
}
