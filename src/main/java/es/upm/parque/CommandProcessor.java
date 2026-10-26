package es.upm.parque;

/**
 * Interprets one text command, split on ';', and returns the response line.
 */
public class CommandProcessor {

    private final UserManager userManager;

    public CommandProcessor(UserManager userManager) {
        this.userManager = userManager;
    }

    public String process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String operation = parts[0].toUpperCase();
        try {
            return switch (operation) {
                case "USER CREATE" -> {
                    requireParams(parts, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                    userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    yield "OK USER CREATED";
                }
                case "USER DELETE" -> {
                    requireParams(parts, 2, "USER DELETE;Correo;Contraseña");
                    userManager.delete(parts[1], parts[2]);
                    yield "OK USER DELETED";
                }
                case "USER UPDATE" -> {
                    requireParams(parts, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                    userManager.update(parts[1], parts[2], parts[3], parts[4]);
                    yield "OK USER UPDATED";
                }
                default -> "ERROR Unknown command: " + parts[0];
            };
        } catch (UserException e) {
            return "ERROR " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR Unexpected error: " + e.getMessage();
        }
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        if (parts.length - 1 != expected) {
            throw new UserException("Wrong number of parameters (expected " + expected + ", got "
                    + (parts.length - 1) + "). Usage: " + usage);
        }
    }
}
