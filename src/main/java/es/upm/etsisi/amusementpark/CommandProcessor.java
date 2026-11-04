package es.upm.etsisi.amusementpark;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interprets text commands such as {@code USER CREATE;...}. Components are
 * separated with {@link String#split}. Every command yields exactly one
 * result line starting with {@code OK} or {@code ERROR}; this class never
 * throws for bad input.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final int CREATE_PARAMS = 7;
    private static final int DELETE_PARAMS = 2;
    private static final int UPDATE_PARAMS = 4;

    /** Syntax of every available command, shown by {@code HELP}. Extend when adding commands. */
    static final List<String> HELP_LINES = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "HELP",
            "EXIT");

    private final UserManager users;
    private boolean exitRequested;

    public CommandProcessor(UserManager users) {
        this.users = users;
    }

    /** True once an {@code EXIT} command has been processed. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Processes one command line.
     *
     * @return the result line, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(SEPARATOR, -1);
        String operation = normalizeOperation(parts[0]);
        String[] params = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            params[i - 1] = parts[i].trim();
        }
        try {
            return switch (operation) {
                case "USER CREATE" -> create(params);
                case "USER DELETE" -> delete(params);
                case "USER UPDATE" -> update(params);
                case "HELP" -> help(params);
                case "EXIT" -> exit(params);
                default -> error("Unknown command: " + parts[0].trim());
            };
        } catch (UserException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected problem processing the command: " + e.getMessage());
        }
    }

    private static String help(String[] p) throws UserException {
        requireParams(p, 0, "HELP");
        StringBuilder sb = new StringBuilder(ok("Available commands:"));
        for (String line : HELP_LINES) {
            sb.append(System.lineSeparator()).append("  ").append(line);
        }
        return sb.toString();
    }

    private String exit(String[] p) throws UserException {
        requireParams(p, 0, "EXIT");
        exitRequested = true;
        return ok("Bye");
    }

    private String create(String[] p) throws UserException {
        requireParams(p, CREATE_PARAMS, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        users.create(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
        return ok("User " + p[3] + " created");
    }

    private String delete(String[] p) throws UserException {
        requireParams(p, DELETE_PARAMS, "USER DELETE;Correo;Contraseña");
        users.delete(p[0], p[1]);
        return ok("User " + p[0] + " deleted");
    }

    private String update(String[] p) throws UserException {
        requireParams(p, UPDATE_PARAMS, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = parseField(p[2]);
        users.update(p[0], p[1], field, p[3]);
        return ok("User " + p[0] + " updated: " + field);
    }

    private static UserField parseField(String text) throws UserException {
        String name = text.toUpperCase(Locale.ROOT);
        if ("DNI".equals(name)) {
            throw new UserException("The DNI cannot be modified");
        }
        for (UserField f : UserField.values()) {
            if (f.name().equals(name)) {
                return f;
            }
        }
        throw new UserException("Unknown field: " + text);
    }

    private static void requireParams(String[] params, int expected, String format) throws UserException {
        if (params.length != expected) {
            throw new UserException("Expected " + expected + " parameters, got " + params.length
                    + ". Format: " + format);
        }
    }

    /** Joins the words of the operation with single spaces and upper-cases them. */
    private static String normalizeOperation(String raw) {
        List<String> words = new ArrayList<>();
        for (String w : raw.trim().split(" ")) {
            if (!w.isEmpty()) {
                words.add(w.toUpperCase(Locale.ROOT));
            }
        }
        return String.join(" ", words);
    }

    private static String ok(String message) {
        return "OK: " + message;
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
