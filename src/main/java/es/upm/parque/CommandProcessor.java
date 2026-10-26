package es.upm.parque;

/**
 * Interprets one text command, executes it and returns the response line.
 * Commands have the form {@code OPERATION;param1;param2;...}.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private final UserManager userManager;

    public CommandProcessor(UserManager userManager) {
        this.userManager = userManager;
    }

    public String process(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            return switch (operation) {
                case "USER CREATE" -> createUser(parts);
                case "USER DELETE" -> deleteUser(parts);
                case "USER UPDATE" -> updateUser(parts);
                default -> "ERROR Unknown command: " + operation;
            };
        } catch (UserException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != 8) {
            return usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "OK USER CREATED";
    }

    private String deleteUser(String[] parts) {
        if (parts.length != 3) {
            return usage("USER DELETE;Correo;Contraseña");
        }
        userManager.delete(parts[1], parts[2]);
        return "OK USER DELETED";
    }

    private String updateUser(String[] parts) {
        if (parts.length != 5) {
            return usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        userManager.update(parts[1], parts[2], parts[3], parts[4]);
        return "OK USER UPDATED";
    }

    private static String usage(String syntax) {
        return "ERROR Wrong number of parameters. Usage: " + syntax;
    }
}
