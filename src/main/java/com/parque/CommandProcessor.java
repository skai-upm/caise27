package com.parque;

/**
 * Interprets text commands ({@code OPERATION;param;param...}) using String.split and answers with
 * one line starting with {@code OK:} or {@code ERROR:}. No exception escapes {@link #process(String)}.
 */
public class CommandProcessor {
    private final UserService users;

    public CommandProcessor(UserService users) {
        this.users = users;
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
        String[] words = parts[0].trim().split(" ");
        if (words.length != 2 || !words[0].equals("USER")) {
            throw new IllegalArgumentException("Unknown command: " + parts[0].trim());
        }
        switch (words[1]) {
            case "CREATE" -> {
                expectParameters(parts, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return "User created";
            }
            case "DELETE" -> {
                expectParameters(parts, 2, "USER DELETE;Correo;Contraseña");
                users.delete(parts[1], parts[2]);
                return "User deleted";
            }
            case "UPDATE" -> {
                expectParameters(parts, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                UserField field = UserField.fromName(parts[3]);
                if (field == null) {
                    throw new IllegalArgumentException("Field cannot be updated: " + parts[3]
                            + " (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                }
                users.update(parts[1], parts[2], field, parts[4]);
                return "User updated";
            }
            default -> throw new IllegalArgumentException("Unknown command: " + parts[0].trim());
        }
    }

    private static void expectParameters(String[] parts, int expected, String format) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("Wrong number of parameters. Expected format: " + format);
        }
    }
}
