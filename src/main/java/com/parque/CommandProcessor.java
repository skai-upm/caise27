package com.parque;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Interprets text commands ({@code OPERATION;param;param...}) using String.split and answers with
 * a response whose first line starts with {@code OK:} or {@code ERROR:}. No exception escapes
 * {@link #process(String)}. The same instance serves both interactive and file input.
 * <p>
 * It is the application's main controller and owns the single current system date of the execution.
 */
public class CommandProcessor {
    /** Syntax of every available command, shown by HELP. Add new commands here. */
    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Identificador;Nombre",
            "GROUP ADD;Identificador;Correo",
            "GROUP REMOVE;Identificador;Correo",
            "GROUP DELETE;Identificador",
            "SYSTEM DATE;yyyy-MM-dd",
            "HELP",
            "EXIT");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserService users;
    private final GroupService groups;
    private boolean exitRequested;
    private LocalDate currentDate;

    /** Creates the controller with the operating system's local date as the current date. */
    public CommandProcessor(UserService users, GroupService groups) {
        this(users, groups, LocalDate.now());
    }

    public CommandProcessor(UserService users, GroupService groups, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.currentDate = initialDate;
    }

    /** The single current date of the execution; every date-dependent rule must use it. */
    public LocalDate getCurrentDate() {
        return currentDate;
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
        switch (operation) {
            case "SYSTEM DATE" -> {
                expectParameters(parts, 1, "SYSTEM DATE;yyyy-MM-dd");
                currentDate = parseDate(parts[1]);
                return "System date set to " + currentDate;
            }
            case "USER CREATE" -> {
                expectParameters(parts, 7, COMMAND_SYNTAX.get(0));
                users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return "User created";
            }
            case "USER DELETE" -> {
                expectParameters(parts, 2, COMMAND_SYNTAX.get(1));
                User deleted = users.delete(parts[1], parts[2]);
                groups.removeUserFromAllGroups(deleted);
                return "User deleted";
            }
            case "USER UPDATE" -> {
                expectParameters(parts, 4, COMMAND_SYNTAX.get(2));
                UserField field = UserField.fromName(parts[3]);
                if (field == null) {
                    throw new IllegalArgumentException("Field cannot be updated: " + parts[3]
                            + " (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                }
                users.update(parts[1], parts[2], field, parts[4]);
                return "User updated";
            }
            case "GROUP CREATE" -> {
                expectParameters(parts, 2, "GROUP CREATE;Identificador;Nombre");
                groups.create(parts[1], parts[2]);
                return "Group created";
            }
            case "GROUP ADD" -> {
                expectParameters(parts, 2, "GROUP ADD;Identificador;Correo");
                groups.addMember(parts[1], parts[2]);
                return "User added to group";
            }
            case "GROUP REMOVE" -> {
                expectParameters(parts, 2, "GROUP REMOVE;Identificador;Correo");
                groups.removeMember(parts[1], parts[2]);
                return "User removed from group";
            }
            case "GROUP DELETE" -> {
                expectParameters(parts, 1, "GROUP DELETE;Identificador");
                groups.delete(parts[1]);
                return "Group deleted";
            }
            default -> throw new IllegalArgumentException("Unknown command: " + operation);
        }
    }

    private static LocalDate parseDate(String value) {
        try {
            if (value.length() != 10) {
                throw new DateTimeParseException("wrong length", value, 0);
            }
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date: '" + value + "' (expected a valid date as yyyy-MM-dd)");
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
