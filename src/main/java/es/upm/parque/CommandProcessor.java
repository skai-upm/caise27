package es.upm.parque;

import java.util.List;

/**
 * Interprets one text command, executes it and returns the response line.
 * Commands have the form {@code OPERATION;param1;param2;...}.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private static final String USER_CREATE_SYNTAX = "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta";
    private static final String USER_DELETE_SYNTAX = "USER DELETE;Correo;Contraseña";
    private static final String USER_UPDATE_SYNTAX = "USER UPDATE;Correo;Contraseña;Campo;NuevoValor";
    private static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;yyyy-MM-dd";

    /** Syntax of every available command, shown by HELP. */
    private static final List<String> COMMAND_SYNTAX = List.of(
            USER_CREATE_SYNTAX,
            USER_DELETE_SYNTAX,
            USER_UPDATE_SYNTAX,
            SYSTEM_DATE_SYNTAX,
            "HELP",
            "EXIT");

    private final UserManager userManager;
    private final SystemDate systemDate;
    private boolean exitRequested;

    public CommandProcessor(UserManager userManager, SystemDate systemDate) {
        this.userManager = userManager;
        this.systemDate = systemDate;
    }

    public String process(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            return switch (operation) {
                case "USER CREATE" -> createUser(parts);
                case "USER DELETE" -> deleteUser(parts);
                case "USER UPDATE" -> updateUser(parts);
                case "SYSTEM DATE" -> setSystemDate(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> "ERROR Unknown command: " + operation;
            };
        } catch (UserException | IllegalArgumentException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != 8) {
            return usage(USER_CREATE_SYNTAX);
        }
        userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "OK USER CREATED";
    }

    private String deleteUser(String[] parts) {
        if (parts.length != 3) {
            return usage(USER_DELETE_SYNTAX);
        }
        userManager.delete(parts[1], parts[2]);
        return "OK USER DELETED";
    }

    private String updateUser(String[] parts) {
        if (parts.length != 5) {
            return usage(USER_UPDATE_SYNTAX);
        }
        userManager.update(parts[1], parts[2], parts[3], parts[4]);
        return "OK USER UPDATED";
    }

    private String setSystemDate(String[] parts) {
        if (parts.length != 2) {
            return usage(SYSTEM_DATE_SYNTAX);
        }
        systemDate.set(SystemDate.parse(parts[1].trim()));
        return "OK SYSTEM DATE " + SystemDate.format(systemDate.get());
    }

    private String help(String[] parts) {
        if (parts.length != 1) {
            return usage("HELP");
        }
        return "Available commands:" + System.lineSeparator()
                + String.join(System.lineSeparator(), COMMAND_SYNTAX);
    }

    private String exit(String[] parts) {
        if (parts.length != 1) {
            return usage("EXIT");
        }
        exitRequested = true;
        return "OK EXIT";
    }

    /**
     * Tells whether EXIT has been processed, so no further commands must be executed.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private static String usage(String syntax) {
        return "ERROR Wrong number of parameters. Usage: " + syntax;
    }
}
