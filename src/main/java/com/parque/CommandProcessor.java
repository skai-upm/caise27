package com.parque;

import java.util.List;

/**
 * Interprets text commands ({@code OPERATION;param;param...}) using String.split and answers with
 * a response whose first line starts with {@code OK:} or {@code ERROR:}. No exception escapes
 * {@link #process(String)}. The same instance serves both interactive and file input.
 */
public class CommandProcessor {
    /** Syntax of every available command, shown by HELP. Add new commands here. */
    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "HELP",
            "EXIT");

    private final UserService users;
    private boolean exitRequested;

    public CommandProcessor(UserService users) {
        this.users = users;
    }

    /** True once an EXIT command has been processed; no further commands must be executed. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    public String process(String line) {
        try {
            return "OK: " + execute(line);
        } catch (RuntimeException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    private String execute(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("Empty command");
        }
        String[] parts = line.split(";", -1);
        String operation = parts[0].trim();
        if (operation.equals("HELP")) {
            expectParameters(parts, 0, "HELP");
            return help();
        }
        if (operation.equals("EXIT")) {
            expectParameters(parts, 0, "EXIT");
            exitRequested = true;
            return "Exiting";
        }
        String[] words = operation.split(" ");
        if (words.length != 2 || !words[0].equals("USER")) {
            throw new IllegalArgumentException("Unknown command: " + operation);
        }
        switch (words[1]) {
            case "CREATE" -> {
                expectParameters(parts, 7, COMMAND_SYNTAX.get(0));
                users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return "User created";
            }
            case "DELETE" -> {
                expectParameters(parts, 2, COMMAND_SYNTAX.get(1));
                users.delete(parts[1], parts[2]);
                return "User deleted";
            }
            case "UPDATE" -> {
                expectParameters(parts, 4, COMMAND_SYNTAX.get(2));
                UserField field = UserField.fromName(parts[3]);
                if (field == null) {
                    throw new IllegalArgumentException("Field cannot be updated: " + parts[3]
                            + " (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                }
                users.update(parts[1], parts[2], field, parts[4]);
                return "User updated";
            }
            default -> throw new IllegalArgumentException("Unknown command: " + operation);
        }
    }

    private static String help() {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (String syntax : COMMAND_SYNTAX) {
            sb.append(System.lineSeparator()).append("  ").append(syntax);
        }
        return sb.toString();
    }

    private static void expectParameters(String[] parts, int expected, String format) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("Wrong number of parameters. Expected format: " + format);
        }
    }
}
