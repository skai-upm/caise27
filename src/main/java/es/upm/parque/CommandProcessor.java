package es.upm.parque;

import java.io.PrintStream;
import java.util.Arrays;

/**
 * Interprets one text command per call. Shared by interactive and file modes.
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
    private final PrintStream out;

    public CommandProcessor(UserManager userManager, PrintStream out) {
        this.userManager = userManager;
        this.out = out;
    }

    /**
     * Processes a command line and prints its result.
     *
     * @return false when the command was EXIT, true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String command = parts[0].trim().toUpperCase();
        String[] params = Arrays.copyOfRange(parts, 1, parts.length);
        for (int i = 0; i < params.length; i++) {
            params[i] = params[i].trim();
        }
        try {
            switch (command) {
                case "USER CREATE" -> {
                    requireParams(command, params, 7);
                    userManager.create(params[0], params[1], params[2], params[3],
                            params[4], params[5], params[6]);
                    out.println("OK USER CREATED");
                }
                case "USER DELETE" -> {
                    requireParams(command, params, 2);
                    userManager.delete(params[0], params[1]);
                    out.println("OK USER DELETED");
                }
                case "USER UPDATE" -> {
                    requireParams(command, params, 4);
                    userManager.update(params[0], params[1], params[2], params[3]);
                    out.println("OK USER UPDATED");
                }
                case "HELP" -> {
                    requireParams(command, params, 0);
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireParams(command, params, 0);
                    out.println("OK EXIT");
                    return false;
                }
                default -> out.println("ERROR Unknown command: " + parts[0].trim() + " (type HELP)");
            }
        } catch (IllegalArgumentException e) {
            out.println("ERROR " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR Unexpected failure: " + e.getMessage());
        }
        return true;
    }

    private static void requireParams(String command, String[] params, int expected) {
        if (params.length != expected) {
            throw new IllegalArgumentException(command + " expects " + expected
                    + " parameter(s) but received " + params.length);
        }
    }
}
