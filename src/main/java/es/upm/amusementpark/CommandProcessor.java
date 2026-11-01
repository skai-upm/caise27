package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Interprets and executes text commands. Commands are split on ';' into the
 * operation keyword and its parameters. Interactive and file input both go
 * through {@link #run(BufferedReader, PrintStream)}.
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

    public CommandProcessor(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Reads commands line by line, executing each nonblank one and printing its
     * result, until EXIT is processed or the input ends.
     */
    public void run(BufferedReader input, PrintStream out) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            CommandResult result = execute(line);
            out.println(result.output());
            if (result.exit()) {
                return;
            }
        }
    }

    /** Executes one command line. */
    public CommandResult execute(String line) {
        String[] parts = line.trim().split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            return switch (parts[0]) {
                case "USER CREATE" -> CommandResult.message(createUser(parts));
                case "USER DELETE" -> CommandResult.message(deleteUser(parts));
                case "USER UPDATE" -> CommandResult.message(updateUser(parts));
                case "HELP" -> CommandResult.message(HELP_TEXT);
                case "EXIT" -> new CommandResult("OK: exiting", true);
                default -> CommandResult.message(
                        "ERROR: unknown command '" + parts[0] + "'. Type HELP to list the commands");
            };
        } catch (UserOperationException e) {
            return CommandResult.message("ERROR: " + e.getMessage());
        }
    }

    private String createUser(String[] parts) {
        requireParameters(parts, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "OK: user " + parts[4] + " created";
    }

    private String deleteUser(String[] parts) {
        requireParameters(parts, 2, "USER DELETE;Correo;Contraseña");
        userManager.delete(parts[1], parts[2]);
        return "OK: user " + parts[1] + " deleted";
    }

    private String updateUser(String[] parts) {
        requireParameters(parts, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = UserField.fromName(parts[3]);
        if (field == null) {
            throw new UserOperationException("field '" + parts[3]
                    + "' cannot be updated; use NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD");
        }
        userManager.update(parts[1], parts[2], field, parts[4]);
        return "OK: user " + parts[1] + " updated";
    }

    private static void requireParameters(String[] parts, int count, String syntax) {
        if (parts.length != count + 1) {
            throw new UserOperationException("expected " + count + " parameters. Usage: " + syntax);
        }
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                throw new UserOperationException("parameter " + i + " is empty. Usage: " + syntax);
            }
        }
    }
}
