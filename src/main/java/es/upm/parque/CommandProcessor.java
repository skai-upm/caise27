package es.upm.parque;

import java.io.UncheckedIOException;

/**
 * Interprets one text command and executes it. Shared by interactive and file modes.
 */
public class CommandProcessor {

    /**
     * Outcome of processing a command: the text to show and whether processing must stop.
     */
    public record Result(String output, boolean exit) {

        static Result ok(String output) {
            return new Result(output, false);
        }

        static Result error(String message) {
            return new Result("ERROR: " + message, false);
        }
    }

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

    public Result process(String line) {
        String[] parts = line.trim().split(";", -1);
        String command = parts[0].trim();
        try {
            return switch (command) {
                case "USER CREATE" -> createUser(parts);
                case "USER DELETE" -> deleteUser(parts);
                case "USER UPDATE" -> updateUser(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> Result.error("unknown command '" + command + "'. Type HELP to list the commands");
            };
        } catch (UserException e) {
            return Result.error(e.getMessage());
        } catch (UncheckedIOException e) {
            return Result.error("storage failure: " + e.getMessage());
        }
    }

    private Result createUser(String[] parts) {
        if (parts.length != 8) {
            return wrongParameters("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return Result.ok("OK USER CREATED");
    }

    private Result deleteUser(String[] parts) {
        if (parts.length != 3) {
            return wrongParameters("USER DELETE;Correo;Contraseña");
        }
        userManager.delete(parts[1], parts[2]);
        return Result.ok("OK USER DELETED");
    }

    private Result updateUser(String[] parts) {
        if (parts.length != 5) {
            return wrongParameters("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field = UserField.fromName(parts[3]);
        if (field == null) {
            return Result.error("field '" + parts[3] + "' cannot be updated. Valid fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD");
        }
        userManager.update(parts[1], parts[2], field, parts[4]);
        return Result.ok("OK USER UPDATED");
    }

    private Result help(String[] parts) {
        if (parts.length != 1) {
            return wrongParameters("HELP");
        }
        return Result.ok(HELP_TEXT);
    }

    private Result exit(String[] parts) {
        if (parts.length != 1) {
            return wrongParameters("EXIT");
        }
        return new Result("OK EXIT", true);
    }

    private static Result wrongParameters(String usage) {
        return Result.error("wrong number of parameters. Usage: " + usage);
    }
}
