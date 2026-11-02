package es.upm.etsisi.amusementpark.command;

import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserException;
import es.upm.etsisi.amusementpark.user.UserField;
import es.upm.etsisi.amusementpark.user.UserService;
import java.util.Optional;

/**
 * Interprets single {@code ;}-separated command lines and delegates to the user service.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "  <field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
            "HELP",
            "EXIT");

    private static final String SEPARATOR = ";";

    private final UserService users;

    public CommandProcessor(UserService users) {
        this.users = users;
    }

    public CommandResult process(String line) {
        String[] parts = line.trim().split(SEPARATOR, -1);
        try {
            return switch (parts[0]) {
                case "USER CREATE" -> create(parts);
                case "USER DELETE" -> delete(parts);
                case "USER UPDATE" -> update(parts);
                case "HELP" -> expectArity(parts, 1, "HELP", CommandResult.message(HELP_TEXT));
                case "EXIT" -> expectArity(parts, 1, "EXIT", new CommandResult("BYE", true));
                default -> CommandResult.error("unknown command '" + parts[0] + "' (type HELP)");
            };
        } catch (UserException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    private CommandResult create(String[] p) throws UserException {
        requireArity(p, 8, "USER CREATE;FirstName;Surnames;DNI;Email;Password;Phone;Card");
        users.create(new User(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
        return CommandResult.message("USER CREATED");
    }

    private CommandResult delete(String[] p) throws UserException {
        requireArity(p, 3, "USER DELETE;Email;Password");
        users.delete(p[1], p[2]);
        return CommandResult.message("USER DELETED");
    }

    private CommandResult update(String[] p) throws UserException {
        requireArity(p, 5, "USER UPDATE;Email;Password;Field;NewValue");
        Optional<UserField> field = UserField.fromName(p[3]);
        if (field.isEmpty()) {
            throw new UserException("field '" + p[3] + "' cannot be modified");
        }
        users.update(p[1], p[2], field.get(), p[4]);
        return CommandResult.message("USER UPDATED");
    }

    private static CommandResult expectArity(String[] p, int n, String usage, CommandResult ok)
            throws UserException {
        requireArity(p, n, usage);
        return ok;
    }

    private static void requireArity(String[] p, int n, String usage) throws UserException {
        if (p.length != n) {
            throw new UserException("wrong number of arguments, usage: " + usage);
        }
    }
}
