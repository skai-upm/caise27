package es.upm.etsisi.amusementpark;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/**
 * Interprets API-like text commands, split with {@link String#split}, and returns a one-line response
 * starting with {@code OK:} or {@code ERROR:}. Never throws for invalid input.
 * It is the application's main controller and holds the single current system date of the execution.
 *
 * <pre>
 * USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta
 * USER DELETE;Correo;Contraseña
 * USER UPDATE;Correo;Contraseña;Campo;NuevoValor
 * GROUP CREATE;Id;Nombre
 * GROUP ADD;Id;Correo
 * GROUP REMOVE;Id;Correo
 * GROUP DELETE;Id
 * SYSTEM DATE;yyyy-MM-dd
 * HELP
 * EXIT
 * </pre>
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";
    private static final int CREATE_TOKENS = 8;
    private static final int DELETE_TOKENS = 3;
    private static final int UPDATE_TOKENS = 5;
    private static final int DATE_TOKENS = 2;
    private static final int GROUP_CREATE_TOKENS = 3;
    private static final int GROUP_MEMBER_TOKENS = 3;
    private static final int GROUP_DELETE_TOKENS = 2;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Syntax of every available command, shown by HELP. Extend it when new commands are added. */
    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "SYSTEM DATE;yyyy-MM-dd",
            "HELP",
            "EXIT");

    private final UserService users;
    private final GroupService groups;
    private boolean exitRequested;
    private LocalDate currentDate;

    /** Creates the controller with the operating system's local date as current date. */
    public CommandProcessor(UserService users, GroupService groups) {
        this(users, groups, LocalDate.now());
    }

    public CommandProcessor(UserService users, GroupService groups, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.currentDate = initialDate;
    }

    /** The single current date of the execution; every time-dependent rule must use it. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /** True once EXIT has been processed: no further command must be executed. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    public String process(String line) {
        if (line == null || line.isBlank()) {
            return error("Empty command");
        }
        String[] tokens = line.split(PARAM_SEPARATOR, -1);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        String operation = tokens[0].toUpperCase(Locale.ROOT);
        try {
            return switch (operation) {
                case "USER CREATE" -> create(tokens);
                case "USER DELETE" -> delete(tokens);
                case "USER UPDATE" -> update(tokens);
                case "GROUP CREATE" -> groupCreate(tokens);
                case "GROUP ADD" -> groupAdd(tokens);
                case "GROUP REMOVE" -> groupRemove(tokens);
                case "GROUP DELETE" -> groupDelete(tokens);
                case "SYSTEM DATE" -> systemDate(tokens);
                case "HELP" -> tokens.length == 1 ? help() : usage("HELP");
                case "EXIT" -> tokens.length == 1 ? exit() : usage("EXIT");
                default -> error("Unknown command: " + tokens[0]);
            };
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Operation failed: " + e.getMessage());
        }
    }

    private String create(String[] t) {
        if (t.length != CREATE_TOKENS) {
            return usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        User u = users.create(t[1], t[2], t[3], t[4], t[5], t[6], t[7]);
        return "OK: User " + u.getEmail() + " created";
    }

    private String delete(String[] t) {
        if (t.length != DELETE_TOKENS) {
            return usage("USER DELETE;Correo;Contraseña");
        }
        User deleted = users.delete(t[1], t[2]);
        groups.removeUserFromAll(deleted);
        return "OK: User " + t[1] + " deleted";
    }

    private String update(String[] t) {
        if (t.length != UPDATE_TOKENS) {
            return usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field = UserField.parse(t[3]);
        if (field == null) {
            return error("Field " + t[3] + " cannot be updated (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        }
        users.update(t[1], t[2], field, t[4]);
        return "OK: User " + t[1] + " updated (" + field + ")";
    }

    private String groupCreate(String[] t) {
        if (t.length != GROUP_CREATE_TOKENS) {
            return usage("GROUP CREATE;Id;Nombre");
        }
        groups.create(t[1], t[2]);
        return "OK: Group " + t[1] + " created";
    }

    private String groupAdd(String[] t) {
        if (t.length != GROUP_MEMBER_TOKENS) {
            return usage("GROUP ADD;Id;Correo");
        }
        groups.addMember(t[1], t[2]);
        return "OK: User " + t[2] + " added to group " + t[1];
    }

    private String groupRemove(String[] t) {
        if (t.length != GROUP_MEMBER_TOKENS) {
            return usage("GROUP REMOVE;Id;Correo");
        }
        groups.removeMember(t[1], t[2]);
        return "OK: User " + t[2] + " removed from group " + t[1];
    }

    private String groupDelete(String[] t) {
        if (t.length != GROUP_DELETE_TOKENS) {
            return usage("GROUP DELETE;Id");
        }
        groups.delete(t[1]);
        return "OK: Group " + t[1] + " deleted";
    }

    private String systemDate(String[] t) {
        if (t.length != DATE_TOKENS) {
            return usage("SYSTEM DATE;yyyy-MM-dd");
        }
        try {
            currentDate = LocalDate.parse(t[1], DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return error("Invalid date " + t[1] + ". Expected format yyyy-MM-dd");
        }
        return "OK: System date set to " + currentDate;
    }

    private static String help() {
        return "Available commands:" + System.lineSeparator() + "  "
                + String.join(System.lineSeparator() + "  ", COMMAND_SYNTAX);
    }

    private String exit() {
        exitRequested = true;
        return "OK: Bye";
    }

    private static String usage(String format) {
        return error("Wrong number of parameters. Usage: " + format);
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
