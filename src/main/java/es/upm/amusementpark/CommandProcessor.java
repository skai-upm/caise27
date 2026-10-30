package es.upm.amusementpark;

import java.io.PrintStream;

/**
 * Interprets and executes text commands. Shared by every input mode.
 */
public class CommandProcessor {

    private final UserService userService;
    private final PrintStream out;

    public CommandProcessor(UserService userService, PrintStream out) {
        this.userService = userService;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} when processing must stop, {@code true} otherwise
     */
    public boolean process(String line) {
        try {
            return execute(line.trim());
        } catch (RuntimeException e) {
            out.println("ERROR: could not process command: " + line);
            return true;
        }
    }

    private boolean execute(String line) {
        String[] parts = line.split(";", -1);
        String operation = parts[0].trim();
        switch (operation) {
            case "USER CREATE":
                if (checkParams(parts, 8, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta")) {
                    out.println(userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]));
                }
                return true;
            case "USER DELETE":
                if (checkParams(parts, 3, "USER DELETE;Correo;Contraseña")) {
                    out.println(userService.delete(parts[1], parts[2]));
                }
                return true;
            case "USER UPDATE":
                if (checkParams(parts, 5, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor")) {
                    out.println(userService.update(parts[1], parts[2], parts[3], parts[4]));
                }
                return true;
            case "HELP":
                if (checkParams(parts, 1, "HELP")) {
                    printHelp();
                }
                return true;
            case "EXIT":
                if (checkParams(parts, 1, "EXIT")) {
                    out.println("Bye");
                    return false;
                }
                return true;
            default:
                out.println("ERROR: unknown command: " + line);
                return true;
        }
    }

    private void printHelp() {
        out.println("Available commands:");
        out.println("  USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        out.println("  USER DELETE;Correo;Contraseña");
        out.println("  USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        out.println("  HELP");
        out.println("  EXIT");
    }

    private boolean checkParams(String[] parts, int expected, String syntax) {
        if (parts.length != expected) {
            out.println("ERROR: wrong number of parameters. Usage: " + syntax);
            return false;
        }
        return true;
    }
}
