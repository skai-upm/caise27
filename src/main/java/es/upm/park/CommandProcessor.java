package es.upm.park;

import java.util.ArrayList;
import java.util.List;

/**
 * Interprets API-like text commands. A command is split on {@code ;} into the operation and
 * its parameters; the operation is split on spaces into its words. Invalid input always
 * produces an error result and never throws.
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";
    private static final String WORD_SEPARATOR = " ";

    /** Syntax of every available command, shown by {@code HELP}. Add new commands here. */
    private static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "HELP",
            "EXIT");

    private final UserManager users;

    public CommandProcessor(UserManager users) {
        this.users = users;
    }

    public Result process(String line) {
        if (line == null || line.isBlank()) {
            return Result.error("Empty command.");
        }
        String[] parts = line.split(PARAM_SEPARATOR, -1);
        String[] params = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            params[i - 1] = parts[i].trim();
        }
        List<String> words = new ArrayList<>();
        for (String w : parts[0].trim().split(WORD_SEPARATOR)) {
            if (!w.isEmpty()) {
                words.add(w);
            }
        }
        if (words.size() == 1 && words.get(0).equals("HELP")) {
            return params.length == 0 ? help() : Result.error("Usage: HELP");
        }
        if (words.size() == 1 && words.get(0).equals("EXIT")) {
            return params.length == 0 ? Result.exit("Bye.") : Result.error("Usage: EXIT");
        }
        if (words.size() != 2 || !words.get(0).equals("USER")) {
            return Result.error("Unknown command: " + parts[0].trim());
        }
        return switch (words.get(1)) {
            case "CREATE" -> userCreate(params);
            case "DELETE" -> userDelete(params);
            case "UPDATE" -> userUpdate(params);
            default -> Result.error("Unknown command: " + parts[0].trim());
        };
    }

    private Result help() {
        return Result.ok("Available commands:" + System.lineSeparator()
                + String.join(System.lineSeparator(), COMMAND_SYNTAX));
    }

    private Result userCreate(String[] p) {
        if (!hasParams(p, 7)) {
            return Result.error("Usage: USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        return users.create(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
    }

    private Result userDelete(String[] p) {
        if (!hasParams(p, 2)) {
            return Result.error("Usage: USER DELETE;Correo;Contraseña");
        }
        return users.delete(p[0], p[1]);
    }

    private Result userUpdate(String[] p) {
        if (!hasParams(p, 4)) {
            return Result.error("Usage: USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field;
        try {
            field = UserField.valueOf(p[2]);
        } catch (IllegalArgumentException e) {
            return Result.error("Field " + p[2] + " cannot be updated. Editable fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD.");
        }
        return users.update(p[0], p[1], field, p[3]);
    }

    /** Exactly {@code count} parameters, none of them empty. */
    private static boolean hasParams(String[] p, int count) {
        if (p.length != count) {
            return false;
        }
        for (String s : p) {
            if (s.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
