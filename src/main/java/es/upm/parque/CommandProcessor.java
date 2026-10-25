package es.upm.parque;

import java.util.Locale;

/**
 * Interprets one text command, split by ';' into operation and parameters, and returns its response.
 */
public class CommandProcessor {

    static final String UNKNOWN_COMMAND = "ERROR UNKNOWN COMMAND";
    static final String WRONG_PARAMETERS = "ERROR WRONG NUMBER OF PARAMETERS";
    static final String UNEXPECTED_ERROR = "ERROR UNEXPECTED FAILURE";

    private final UserManager users;

    public CommandProcessor(UserManager users) {
        this.users = users;
    }

    public String process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String operation = parts[0].toUpperCase(Locale.ROOT);
        try {
            return switch (operation) {
                case "USER CREATE" -> createUser(parts);
                case "USER DELETE" -> deleteUser(parts);
                case "USER UPDATE" -> updateUser(parts);
                default -> UNKNOWN_COMMAND;
            };
        } catch (RuntimeException e) {
            return UNEXPECTED_ERROR;
        }
    }

    private String createUser(String[] p) {
        if (p.length != 8) {
            return WRONG_PARAMETERS;
        }
        return users.create(new User(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
    }

    private String deleteUser(String[] p) {
        if (p.length != 3) {
            return WRONG_PARAMETERS;
        }
        return users.delete(p[1], p[2]);
    }

    private String updateUser(String[] p) {
        if (p.length != 5) {
            return WRONG_PARAMETERS;
        }
        return users.update(p[1], p[2], p[3], p[4]);
    }
}
