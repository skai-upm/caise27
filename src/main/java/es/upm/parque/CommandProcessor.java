package es.upm.parque;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;

/**
 * Interprets text commands such as {@code USER CREATE;Ana;...}. The command is split with
 * {@link String#split} into the operation (first segment) and its parameters (remaining segments).
 * Every command produces a response; no command can make the application stop unexpectedly.
 * The same processor serves interactive and file input through {@link #run}.
 * It is the main controller and owns the single system date used by time-dependent rules.
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";

    private static final String USAGE_USER_CREATE = "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta";
    private static final String USAGE_USER_DELETE = "USER DELETE;Correo;Contraseña";
    private static final String USAGE_USER_UPDATE = "USER UPDATE;Correo;Contraseña;Campo;NuevoValor";
    private static final String USAGE_SYSTEM_DATE = "SYSTEM DATE;yyyy-MM-dd";
    private static final String USAGE_HELP = "HELP";
    private static final String USAGE_EXIT = "EXIT";

    private static final List<String> HELP_LINES = List.of(
        USAGE_USER_CREATE,
        USAGE_USER_DELETE,
        USAGE_USER_UPDATE + "  (Campo: " + Arrays.toString(UserField.values()) + ")",
        USAGE_SYSTEM_DATE,
        USAGE_HELP,
        USAGE_EXIT);

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserManager users;
    private LocalDate currentDate;
    private boolean exitRequested;

    /** Creates the controller with the operating system's current date as system date. */
    public CommandProcessor(UserManager users) {
        this(users, LocalDate.now());
    }

    public CommandProcessor(UserManager users, LocalDate initialDate) {
        this.users = users;
        this.currentDate = initialDate;
    }

    /** The single system date that every time-dependent rule must use. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Reads commands line by line and prints each response until EXIT is executed or the input ends.
     */
    public void run(BufferedReader in, PrintStream out) throws IOException {
        String line;
        while (!exitRequested && (line = in.readLine()) != null) {
            String response = execute(line);
            if (response != null) {
                out.println(response);
            }
        }
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
            if (operation.length == 1) {
                return executeGeneral(operation[0], parts);
            }
            if (operation.length == 2 && operation[0].equals("USER")) {
                return executeUser(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("SYSTEM")) {
                return executeSystem(operation[1], parts);
            }
            return error("Unknown command: " + parts[0].trim());
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected problem: " + e.getMessage());
        }
    }

    private String executeGeneral(String operation, String[] parts) {
        switch (operation) {
            case "HELP" -> {
                requireParams(parts, 0, USAGE_HELP);
                return "Available commands:" + System.lineSeparator() + "  "
                    + String.join(System.lineSeparator() + "  ", HELP_LINES);
            }
            case "EXIT" -> {
                requireParams(parts, 0, USAGE_EXIT);
                exitRequested = true;
                return ok("Bye");
            }
            default -> {
                return error("Unknown command: " + operation);
            }
        }
    }

    private String executeUser(String action, String[] parts) {
        switch (action) {
            case "CREATE" -> {
                requireParams(parts, 7, USAGE_USER_CREATE);
                User u = users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return ok("User " + u.getDni() + " created");
            }
            case "DELETE" -> {
                requireParams(parts, 2, USAGE_USER_DELETE);
                users.delete(parts[1], parts[2]);
                return ok("User " + parts[1] + " deleted");
            }
            case "UPDATE" -> {
                requireParams(parts, 4, USAGE_USER_UPDATE);
                UserField field = parseField(parts[3]);
                users.update(parts[1], parts[2], field, parts[4]);
                return ok("User " + parts[1] + " updated (" + field + ")");
            }
            default -> {
                return error("Unknown command: USER " + action);
            }
        }
    }

    private String executeSystem(String action, String[] parts) {
        if (!action.equals("DATE")) {
            return error("Unknown command: SYSTEM " + action);
        }
        requireParams(parts, 1, USAGE_SYSTEM_DATE);
        try {
            currentDate = LocalDate.parse(parts[1].trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date " + parts[1] + ". Expected format yyyy-MM-dd");
        }
        return ok("System date set to " + currentDate);
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
