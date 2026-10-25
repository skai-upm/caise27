package es.upm.parque;

import java.util.Arrays;

/**
 * Interprets text commands of the form {@code OPERATION;param1;param2;...} and executes them.
 * The same processor is used for interactive input and for command files.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  HELP",
            "  EXIT");

    private final UserManager userManager;
    private boolean finished;

    public CommandProcessor(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Returns true once EXIT has been processed; no further commands should be supplied.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Executes one command line and returns the message to show, or {@code null} for a blank line.
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.trim().split(SEPARATOR, -1);
        String operation = parts[0].trim();
        String[] params = Arrays.copyOfRange(parts, 1, parts.length);
        for (int i = 0; i < params.length; i++) {
            params[i] = params[i].trim();
        }
        switch (operation) {
            case "USER CREATE":
                return userCreate(params);
            case "USER DELETE":
                return userDelete(params);
            case "USER UPDATE":
                return userUpdate(params);
            case "HELP":
                return params.length == 0 ? HELP_TEXT : wrongParameters("HELP");
            case "EXIT":
                if (params.length != 0) {
                    return wrongParameters("EXIT");
                }
                finished = true;
                return "OK EXIT";
            default:
                return "ERROR Unknown command: " + operation + ". Type HELP to see the available commands";
        }
    }

    private String userCreate(String[] p) {
        if (p.length != 7) {
            return wrongParameters("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        return userManager.createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
    }

    private String userDelete(String[] p) {
        if (p.length != 2) {
            return wrongParameters("USER DELETE;Correo;Contraseña");
        }
        return userManager.deleteUser(p[0], p[1]);
    }

    private String userUpdate(String[] p) {
        if (p.length != 4) {
            return wrongParameters("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        if ("DNI".equals(p[2])) {
            return "ERROR The DNI cannot be modified";
        }
        UserField field = UserField.fromName(p[2]);
        if (field == null) {
            return "ERROR Unknown field: " + p[2] + ". Editable fields: " + Arrays.toString(UserField.values());
        }
        return userManager.updateUser(p[0], p[1], field, p[3]);
    }

    private static String wrongParameters(String syntax) {
        return "ERROR Wrong number of parameters. Usage: " + syntax;
    }
}
