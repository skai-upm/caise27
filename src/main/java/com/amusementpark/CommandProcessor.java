package com.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Interprets and executes text commands. Shared by interactive and file modes.
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

    private final UserManager userManager;
    private final PrintStream out;

    public CommandProcessor(UserManager userManager, PrintStream out) {
        this.userManager = userManager;
        this.out = out;
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input) throws IOException {
        String line;
        while ((line = input.readLine()) != null) {
            if (!process(line)) {
                return;
            }
        }
    }

    /**
     * Processes a single command line.
     *
     * @return {@code false} if the command was EXIT, {@code true} otherwise
     */
    public boolean process(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        String[] parts = command.split(";", -1);
        String operation = parts[0].trim();
        switch (operation) {
            case "USER CREATE" -> {
                if (hasParameters(parts, 7)) {
                    out.println(userManager.create(parts[1], parts[2], parts[3], parts[4],
                            parts[5], parts[6], parts[7]));
                }
            }
            case "USER DELETE" -> {
                if (hasParameters(parts, 2)) {
                    out.println(userManager.delete(parts[1], parts[2]));
                }
            }
            case "USER UPDATE" -> {
                if (hasParameters(parts, 4)) {
                    out.println(userManager.update(parts[1], parts[2], parts[3], parts[4]));
                }
            }
            case "HELP" -> {
                if (hasParameters(parts, 0)) {
                    out.println(HELP_TEXT);
                }
            }
            case "EXIT" -> {
                if (hasParameters(parts, 0)) {
                    out.println("OK: Bye.");
                    return false;
                }
            }
            default -> out.println("ERROR: Unknown command: " + operation + " (type HELP for the list)");
        }
        return true;
    }

    private boolean hasParameters(String[] parts, int expected) {
        if (parts.length - 1 == expected) {
            return true;
        }
        out.println("ERROR: " + parts[0].trim() + " expects " + expected
                + " parameter(s) but got " + (parts.length - 1) + ".");
        return false;
    }
}
