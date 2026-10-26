package es.upm.parque;

import java.util.Arrays;

/**
 * Interprets text commands such as {@code USER CREATE;Ana;...}. The command is split with
 * {@link String#split} into the operation (first segment) and its parameters (remaining segments).
 * Every command produces a response line; no command can make the application stop unexpectedly.
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";

    private final UserManager users;

    public CommandProcessor(UserManager users) {
        this.users = users;
    }

    /**
     * Executes one command line.
     *
     * @return the response to show, or {@code null} for a blank line
     */
    public String execute(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.trim().split(PARAM_SEPARATOR, -1);
        String[] operation = parts[0].trim().split(" ");
        try {
            if (operation.length == 2 && operation[0].equals("USER")) {
                return executeUser(operation[1], parts);
            }
            return error("Unknown command: " + parts[0].trim());
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected problem: " + e.getMessage());
        }
    }

    private String executeUser(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                User u = users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return ok("User " + u.getDni() + " created");
            }
            case "DELETE" -> {
                requireParams(parts, 2, "USER DELETE;Correo;Contraseña");
                users.delete(parts[1], parts[2]);
                return ok("User " + parts[1] + " deleted");
            }
            case "UPDATE" -> {
                requireParams(parts, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                UserField field = parseField(parts[3]);
                users.update(parts[1], parts[2], field, parts[4]);
                return ok("User " + parts[1] + " updated (" + field + ")");
            }
            default -> {
                return error("Unknown command: USER " + action);
            }
        }
    }

    private static UserField parseField(String text) {
        try {
            return UserField.valueOf(text.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Field " + text + " cannot be updated. Editable fields: "
                + Arrays.toString(UserField.values()));
        }
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        int given = parts.length - 1;
        if (given != expected) {
            throw new IllegalArgumentException(
                "Expected " + expected + " parameters but got " + given + ". Usage: " + usage);
        }
    }

    private static String ok(String message) {
        return "OK: " + message;
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
