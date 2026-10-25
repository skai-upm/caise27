package com.amusementpark;

import java.io.PrintStream;

/**
 * Interprets one text command at a time. Shared by interactive and file modes.
 */
public class CommandProcessor {
    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor"
                    + "   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  HELP",
            "  EXIT");

    private final UserManager userManager;
    private final PrintStream out;

    public CommandProcessor(UserManager userManager, PrintStream out) {
        this.userManager = userManager;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return false when processing must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        if (line == null || line.isBlank()) {
            return true;
        }
        String[] parts = line.trim().split(";", -1);
        String operation = parts[0].trim();
        switch (operation) {
            case "USER CREATE":
                if (hasParameters(parts, 7)) {
                    out.println(userManager.create(parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7]));
                }
                return true;
            case "USER DELETE":
                if (hasParameters(parts, 2)) {
                    out.println(userManager.delete(parts[1], parts[2]));
                }
                return true;
            case "USER UPDATE":
                if (hasParameters(parts, 4)) {
                    out.println(userManager.update(parts[1], parts[2], parts[3], parts[4]));
                }
                return true;
            case "HELP":
                out.println(HELP_TEXT);
                return true;
            case "EXIT":
                out.println("Bye");
                return false;
            default:
                out.println("ERROR: unknown command: " + operation + " (type HELP for the list)");
                return true;
        }
    }

    private boolean hasParameters(String[] parts, int expected) {
        if (parts.length - 1 != expected) {
            out.println("ERROR: " + parts[0].trim() + " expects " + expected
                    + " parameters but received " + (parts.length - 1));
            return false;
        }
        return true;
    }
}
