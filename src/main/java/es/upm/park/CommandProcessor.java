package es.upm.park;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Interprets text commands of the form {@code OPERATION;param1;param2;...}
 * using {@link String#split(String, int)} and returns one result message.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    /** Syntax of every available command, shown by HELP. Extend when adding commands. */
    static final List<String> USAGE = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "HELP",
            "EXIT");

    private final UserManager users;
    private boolean exitRequested;

    public CommandProcessor(UserManager users) {
        this.users = users;
    }

    public String process(String line) {
        if (line == null || line.isBlank()) {
            return "ERROR: empty command";
        }
        String[] parts = line.split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            return switch (operation) {
                case "USER CREATE" -> create(parts);
                case "USER DELETE" -> delete(parts);
                case "USER UPDATE" -> update(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> "ERROR: unknown command '" + operation + "'";
            };
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: command could not be completed (" + e.getMessage() + ")";
        }
    }

    /** True once EXIT has been processed; input loops must stop reading. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String help(String[] p) {
        requireParams(p, 0, "HELP");
        StringBuilder sb = new StringBuilder("OK: available commands:");
        for (String usage : USAGE) {
            sb.append(System.lineSeparator()).append("  ").append(usage);
        }
        sb.append(System.lineSeparator()).append("  Campo (USER UPDATE): ")
                .append(String.join(", ", Arrays.stream(UserField.values()).map(Enum::name).toList()));
        return sb.toString();
    }

    private String exit(String[] p) {
        requireParams(p, 0, "EXIT");
        exitRequested = true;
        return "OK: application terminated";
    }

    private String create(String[] p) {
        requireParams(p, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        return "OK: user " + p[4] + " created";
    }

    private String delete(String[] p) {
        requireParams(p, 2, "USER DELETE;Correo;Contraseña");
        users.delete(p[1], p[2]);
        return "OK: user " + p[1] + " deleted";
    }

    private String update(String[] p) {
        requireParams(p, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = parseField(p[3]);
        users.update(p[1], p[2], field, p[4]);
        return "OK: user " + p[1] + " updated (" + field + ")";
    }

    private static UserField parseField(String name) {
        try {
            return UserField.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unsupported field '" + name + "'");
        }
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("expected " + expected + " parameters, usage: " + usage);
        }
    }
}
