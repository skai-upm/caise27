package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Interprets and executes text commands. Interactive and file modes both feed
 * their lines through this class, so every command has a single implementation.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "  USER DELETE;Correo;Contraseña",
            "  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "  HELP",
            "  EXIT");

    private final UserManager users;
    private final PrintStream out;

    public CommandProcessor(UserManager users, PrintStream out) {
        this.users = users;
        this.out = out;
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            if (!process(line)) {
                return;
            }
        }
    }

    /**
     * Executes one command line.
     *
     * @return false if the command was EXIT, true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String operation = parts[0].trim();
        String[] params = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            params[i - 1] = parts[i].trim();
        }
        try {
            switch (operation) {
                case "USER CREATE" -> {
                    requireParams(operation, params, 7);
                    users.create(params[0], params[1], params[2], params[3], params[4], params[5], params[6]);
                    out.println("User created: " + params[3]);
                }
                case "USER DELETE" -> {
                    requireParams(operation, params, 2);
                    users.delete(params[0], params[1]);
                    out.println("User deleted: " + params[0]);
                }
                case "USER UPDATE" -> {
                    requireParams(operation, params, 4);
                    users.update(params[0], params[1], params[2], params[3]);
                    out.println("User updated: " + params[2]);
                }
                case "HELP" -> {
                    requireParams(operation, params, 0);
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireParams(operation, params, 0);
                    out.println("Bye");
                    return false;
                }
                default -> out.println("Error: unknown command '" + operation + "'. Type HELP for the list of commands.");
            }
        } catch (UserOperationException e) {
            out.println("Error: " + e.getMessage());
        }
        return true;
    }

    private static void requireParams(String operation, String[] params, int expected)
            throws UserOperationException {
        if (params.length != expected) {
            throw new UserOperationException(operation + " expects " + expected
                    + " parameter(s) but received " + params.length + ". Type HELP for the syntax.");
        }
    }
}
