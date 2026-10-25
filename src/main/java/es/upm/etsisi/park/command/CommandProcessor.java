package es.upm.etsisi.park.command;

import es.upm.etsisi.park.persistence.PersistenceException;
import es.upm.etsisi.park.service.UserException;
import es.upm.etsisi.park.service.UserService;
import java.util.Locale;

/**
 * Parses ';'-separated command lines and dispatches them. Used identically by every input source.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    static final String USER_CREATE = "USER CREATE";
    static final String USER_DELETE = "USER DELETE";
    static final String USER_UPDATE = "USER UPDATE";
    static final String HELP = "HELP";
    static final String EXIT = "EXIT";

    static final String USER_CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    static final String USER_DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";

    private final UserService userService;

    public CommandProcessor(UserService userService) {
        this.userService = userService;
    }

    public CommandResult process(String line) {
        if (line == null || line.isBlank()) {
            return CommandResult.of("");
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String command = normalizeKeyword(parts[0]);
        try {
            return switch (command) {
                case USER_CREATE -> userCreate(parts);
                case USER_DELETE -> userDelete(parts);
                case USER_UPDATE -> userUpdate(parts);
                case HELP -> help();
                case EXIT -> CommandResult.exit("BYE");
                default -> CommandResult.error("Unknown command '" + parts[0] + "'. Type HELP to list commands");
            };
        } catch (UserException e) {
            return CommandResult.error(e.getMessage());
        } catch (PersistenceException e) {
            return CommandResult.error(e.getMessage());
        } catch (RuntimeException e) {
            return CommandResult.error("Unexpected error processing command: " + e.getMessage());
        }
    }

    private CommandResult userCreate(String[] parts) {
        if (parts.length != 8) {
            return usage(USER_CREATE_SYNTAX);
        }
        userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return CommandResult.of("USER CREATED");
    }

    private CommandResult userDelete(String[] parts) {
        if (parts.length != 3) {
            return usage(USER_DELETE_SYNTAX);
        }
        userService.delete(parts[1], parts[2]);
        return CommandResult.of("USER DELETED");
    }

    private CommandResult userUpdate(String[] parts) {
        if (parts.length != 5) {
            return usage(USER_UPDATE_SYNTAX);
        }
        userService.update(parts[1], parts[2], parts[3], parts[4]);
        return CommandResult.of("USER UPDATED");
    }

    private static CommandResult help() {
        String text = String.join(System.lineSeparator(),
                "Available commands:",
                "  " + USER_CREATE_SYNTAX,
                "  " + USER_DELETE_SYNTAX,
                "  " + USER_UPDATE_SYNTAX + "   (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
                "  " + HELP,
                "  " + EXIT);
        return CommandResult.of(text);
    }

    private static CommandResult usage(String syntax) {
        return CommandResult.error("Wrong number of arguments. Usage: " + syntax);
    }

    private static String normalizeKeyword(String keyword) {
        StringBuilder sb = new StringBuilder();
        for (String word : keyword.trim().split(" ")) {
            if (word.isEmpty()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(word.toUpperCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
