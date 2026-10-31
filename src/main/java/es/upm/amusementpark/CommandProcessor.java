package es.upm.amusementpark;

import java.io.IOException;
import java.io.PrintStream;

/**
 * Interprets and executes one text command at a time. Both interactive and
 * file input go through {@link #process(String)}, so a command behaves the
 * same regardless of where it comes from.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "      Campo: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
            "  HELP",
            "  EXIT");

    private static final String SEPARATOR = ";";

    private final UserManager userManager;
    private final PrintStream out;

    public CommandProcessor(UserManager userManager, PrintStream out) {
        this.userManager = userManager;
        this.out = out;
    }

    /**
     * Executes a single command line.
     *
     * @return {@code false} if the command was {@code EXIT} and processing
     *         must stop, {@code true} otherwise
     */
    public boolean process(String line) {
        if (line == null || line.isBlank()) {
            return true;
        }
        String[] parts = line.trim().split(SEPARATOR, -1);
        String operation = parts[0].trim();
        int parameters = parts.length - 1;
        try {
            switch (operation) {
                case "USER CREATE" -> {
                    checkParameters(operation, parameters, 7);
                    userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    out.println("OK: user " + parts[4] + " created");
                }
                case "USER DELETE" -> {
                    checkParameters(operation, parameters, 2);
                    userManager.delete(parts[1], parts[2]);
                    out.println("OK: user " + parts[1] + " deleted");
                }
                case "USER UPDATE" -> {
                    checkParameters(operation, parameters, 4);
                    UserField field = UserField.fromName(parts[3]);
                    if (field == null) {
                        throw new IllegalArgumentException("field " + parts[3] + " cannot be updated");
                    }
                    userManager.update(parts[1], parts[2], field, parts[4]);
                    out.println("OK: user " + parts[1] + " updated");
                }
                case "HELP" -> {
                    checkParameters(operation, parameters, 0);
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    checkParameters(operation, parameters, 0);
                    out.println("Bye.");
                    return false;
                }
                default -> throw new IllegalArgumentException("unknown command '" + operation + "' (type HELP)");
            }
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (IOException e) {
            out.println("ERROR: could not save users: " + e.getMessage());
        }
        return true;
    }

    private static void checkParameters(String operation, int actual, int expected) {
        if (actual != expected) {
            throw new IllegalArgumentException(operation + " expects " + expected
                    + " parameter(s) but received " + actual);
        }
    }
}
