package es.upm.parque;

/**
 * Interprets text commands (components separated by ';') and executes them.
 * Shared by interactive and file execution modes.
 */
public class CommandProcessor {

    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  HELP",
            "  EXIT");

    private final UserManager userManager;
    private boolean exitRequested;

    public CommandProcessor(UserManager userManager) {
        this.userManager = userManager;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Executes one command line and returns the response to show to the user.
     */
    public String process(String line) {
        String[] parts = line.trim().split(";", -1);
        String operation = parts[0].trim().toUpperCase();
        try {
            return switch (operation) {
                case "USER CREATE" -> {
                    if (parts.length != 8) {
                        yield usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                    }
                    yield userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                }
                case "USER DELETE" -> {
                    if (parts.length != 3) {
                        yield usage("USER DELETE;Correo;Contraseña");
                    }
                    yield userManager.delete(parts[1], parts[2]);
                }
                case "USER UPDATE" -> {
                    if (parts.length != 5) {
                        yield usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                    }
                    yield userManager.update(parts[1], parts[2], parts[3], parts[4]);
                }
                case "HELP" -> {
                    if (parts.length != 1) {
                        yield usage("HELP");
                    }
                    yield HELP_TEXT;
                }
                case "EXIT" -> {
                    if (parts.length != 1) {
                        yield usage("EXIT");
                    }
                    exitRequested = true;
                    yield "OK BYE";
                }
                default -> "ERROR unknown command: " + parts[0].trim() + " (type HELP)";
            };
        } catch (Exception e) {
            return "ERROR could not execute command: " + e.getMessage();
        }
    }

    private static String usage(String syntax) {
        return "ERROR wrong number of parameters. Usage: " + syntax;
    }
}
