package es.upm.park.cli;

import es.upm.park.service.UserOperationException;
import es.upm.park.service.UserService;

/**
 * Interprets ';'-separated text commands using basic String operations and
 * dispatches them to the user service. The same instance serves both
 * interactive and file input.
 */
public class CommandProcessor {

    static final String SEPARATOR = ";";

    static final String USER_CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    static final String USER_DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  " + USER_CREATE_SYNTAX,
            "  " + USER_DELETE_SYNTAX,
            "  " + USER_UPDATE_SYNTAX,
            "      <field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD",
            "  HELP",
            "  EXIT");

    private final UserService userService;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    public CommandResult process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return CommandResult.message("");
        }
        String[] fields = line.split(SEPARATOR, -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        String command = normalizeCommandName(fields[0]);
        try {
            return switch (command) {
                case "USER CREATE" -> createUser(fields);
                case "USER DELETE" -> deleteUser(fields);
                case "USER UPDATE" -> updateUser(fields);
                case "HELP" -> hasFieldCount(fields, 1)
                        ? CommandResult.message(HELP_TEXT)
                        : CommandResult.error("Usage: HELP");
                case "EXIT" -> hasFieldCount(fields, 1)
                        ? CommandResult.exit("EXIT")
                        : CommandResult.error("Usage: EXIT");
                default -> CommandResult.error("Unknown command: " + fields[0] + ". Type HELP for the list of commands");
            };
        } catch (UserOperationException e) {
            return CommandResult.error(e.getMessage());
        } catch (RuntimeException e) {
            return CommandResult.error("Unexpected problem processing the command: " + e.getMessage());
        }
    }

    private CommandResult createUser(String[] fields) throws UserOperationException {
        if (!hasFieldCount(fields, 8)) {
            return CommandResult.error("Usage: " + USER_CREATE_SYNTAX);
        }
        userService.create(fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
        return CommandResult.message("USER CREATED");
    }

    private CommandResult deleteUser(String[] fields) throws UserOperationException {
        if (!hasFieldCount(fields, 3)) {
            return CommandResult.error("Usage: " + USER_DELETE_SYNTAX);
        }
        userService.delete(fields[1], fields[2]);
        return CommandResult.message("USER DELETED");
    }

    private CommandResult updateUser(String[] fields) throws UserOperationException {
        if (!hasFieldCount(fields, 5)) {
            return CommandResult.error("Usage: " + USER_UPDATE_SYNTAX);
        }
        userService.update(fields[1], fields[2], fields[3], fields[4]);
        return CommandResult.message("USER UPDATED");
    }

    private static boolean hasFieldCount(String[] fields, int expected) {
        return fields.length == expected;
    }

    /**
     * Upper-cases the command name and collapses runs of whitespace into a
     * single space so that "user   create" is recognised as "USER CREATE".
     */
    static String normalizeCommandName(String raw) {
        StringBuilder normalized = new StringBuilder();
        boolean pendingSpace = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isWhitespace(c)) {
                pendingSpace = normalized.length() > 0;
            } else {
                if (pendingSpace) {
                    normalized.append(' ');
                    pendingSpace = false;
                }
                normalized.append(Character.toUpperCase(c));
            }
        }
        return normalized.toString();
    }
}
