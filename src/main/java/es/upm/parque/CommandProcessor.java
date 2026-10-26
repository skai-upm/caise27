package es.upm.parque;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Interprets text commands of the form {@code OPERATION;param1;param2;...} and returns a response line.
 */
public class CommandProcessor {
    private static final String[] COMMAND_SYNTAX = {
        "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
        "USER DELETE;Correo;Contraseña",
        "USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
        "GROUP CREATE;Id;Nombre",
        "GROUP ADD;Id;Correo",
        "GROUP REMOVE;Id;Correo",
        "GROUP DELETE;Id",
        "SYSTEM DATE;yyyy-MM-dd",
        "HELP",
        "EXIT"
    };

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserManager userManager;
    private final GroupManager groupManager;
    private final SystemDate systemDate;
    private boolean exitRequested;

    public CommandProcessor(UserManager userManager, GroupManager groupManager, SystemDate systemDate) {
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.systemDate = systemDate;
    }

    public String process(String line) {
        String[] parts = line.split(";", -1);
        String[] operation = parts[0].trim().split(" ");
        try {
            if (operation.length == 2 && operation[0].equals("USER")) {
                return processUser(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("GROUP")) {
                return processGroup(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("SYSTEM") && operation[1].equals("DATE")) {
                requireParams(parts, 1, "SYSTEM DATE;yyyy-MM-dd");
                systemDate.set(parseDate(parts[1]));
                return "OK SYSTEM DATE -> " + systemDate.get();
            }
            if (operation.length == 1 && operation[0].equals("HELP")) {
                requireParams(parts, 0, "HELP");
                return help();
            }
            if (operation.length == 1 && operation[0].equals("EXIT")) {
                requireParams(parts, 0, "EXIT");
                exitRequested = true;
                return "OK EXIT";
            }
            return "ERROR: unknown command '" + parts[0].trim() + "'";
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public SystemDate getSystemDate() {
        return systemDate;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    private static String help() {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (String syntax : COMMAND_SYNTAX) {
            sb.append(System.lineSeparator()).append("  ").append(syntax);
        }
        return sb.toString();
    }

    private String processUser(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                User u = userManager.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                return "OK USER CREATED -> " + u;
            }
            case "DELETE" -> {
                requireParams(p, 2, "USER DELETE;Correo;Contraseña");
                User deleted = userManager.delete(p[1], p[2]);
                groupManager.removeUserFromAllGroups(deleted);
                return "OK USER DELETED -> " + p[1];
            }
            case "UPDATE" -> {
                requireParams(p, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                UserField field = parseField(p[3]);
                User u = userManager.update(p[1], p[2], field, p[4]);
                return "OK USER UPDATED -> " + u;
            }
            default -> {
                return "ERROR: unknown command 'USER " + action + "'";
            }
        }
    }

    private String processGroup(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 2, "GROUP CREATE;Id;Nombre");
                return "OK GROUP CREATED -> " + groupManager.create(p[1], p[2]);
            }
            case "ADD" -> {
                requireParams(p, 2, "GROUP ADD;Id;Correo");
                return "OK GROUP MEMBER ADDED -> " + groupManager.addMember(p[1], p[2]);
            }
            case "REMOVE" -> {
                requireParams(p, 2, "GROUP REMOVE;Id;Correo");
                return "OK GROUP MEMBER REMOVED -> " + groupManager.removeMember(p[1], p[2]);
            }
            case "DELETE" -> {
                requireParams(p, 1, "GROUP DELETE;Id");
                return "OK GROUP DELETED -> " + groupManager.delete(p[1]).getId();
            }
            default -> {
                return "ERROR: unknown command 'GROUP " + action + "'";
            }
        }
    }

    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date '" + text + "': expected a real date as yyyy-MM-dd");
        }
    }

    private static UserField parseField(String name) {
        for (UserField f : UserField.values()) {
            if (f.name().equals(name.trim().toUpperCase())) {
                return f;
            }
        }
        throw new IllegalArgumentException("field '" + name + "' cannot be updated; valid fields are NAME, SURNAME,"
                + " EMAIL, PASSWORD, PHONE, CARD");
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException(
                    "expected " + expected + " parameters but got " + (parts.length - 1) + ". Usage: " + usage);
        }
    }
}
