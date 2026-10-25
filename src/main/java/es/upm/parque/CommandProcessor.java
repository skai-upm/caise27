package es.upm.parque;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Interprets one text command, split by ';' into operation and parameters, and returns its response.
 */
public class CommandProcessor {

    static final String UNKNOWN_COMMAND = "ERROR UNKNOWN COMMAND";
    static final String WRONG_PARAMETERS = "ERROR WRONG NUMBER OF PARAMETERS";
    static final String UNEXPECTED_ERROR = "ERROR UNEXPECTED FAILURE";
    static final String EXIT_RESPONSE = "OK EXIT";
    static final String INVALID_DATE = "ERROR INVALID DATE";
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

    private final UserManager users;
    private final GroupManager groups;
    private final SystemDate systemDate;
    private boolean finished;

    public CommandProcessor(UserManager users, GroupManager groups, SystemDate systemDate) {
        this.users = users;
        this.groups = groups;
        this.systemDate = systemDate;
    }

    public String process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String operation = parts[0].toUpperCase(Locale.ROOT);
        try {
            return switch (operation) {
                case "USER CREATE" -> createUser(parts);
                case "USER DELETE" -> deleteUser(parts);
                case "USER UPDATE" -> updateUser(parts);
                case "GROUP CREATE" -> groupCommand(parts, 3, () -> groups.create(parts[1], parts[2]));
                case "GROUP ADD" -> groupCommand(parts, 3, () -> groups.addMember(parts[1], parts[2]));
                case "GROUP REMOVE" -> groupCommand(parts, 3, () -> groups.removeMember(parts[1], parts[2]));
                case "GROUP DELETE" -> groupCommand(parts, 2, () -> groups.delete(parts[1]));
                case "SYSTEM DATE" -> setDate(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> UNKNOWN_COMMAND;
            };
        } catch (RuntimeException e) {
            return UNEXPECTED_ERROR;
        }
    }

    /**
     * True once EXIT has been processed; no further commands should be read.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * The application's single current date, shared with every component that needs it.
     */
    public SystemDate getSystemDate() {
        return systemDate;
    }

    private String setDate(String[] p) {
        if (p.length != 2) {
            return WRONG_PARAMETERS;
        }
        LocalDate date = SystemDate.parse(p[1]);
        if (date == null) {
            return INVALID_DATE;
        }
        systemDate.set(date);
        return "OK SYSTEM DATE -> " + SystemDate.format(date);
    }

    private String help(String[] p) {
        if (p.length != 1) {
            return WRONG_PARAMETERS;
        }
        return String.join(System.lineSeparator(), COMMAND_SYNTAX);
    }

    private String exit(String[] p) {
        if (p.length != 1) {
            return WRONG_PARAMETERS;
        }
        finished = true;
        return EXIT_RESPONSE;
    }

    private String createUser(String[] p) {
        if (p.length != 8) {
            return WRONG_PARAMETERS;
        }
        return users.create(new User(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
    }

    private String deleteUser(String[] p) {
        if (p.length != 3) {
            return WRONG_PARAMETERS;
        }
        User user = users.findByEmail(p[1]);
        String response = users.delete(p[1], p[2]);
        if (UserManager.USER_DELETED.equals(response)) {
            groups.removeUserFromAll(user);
        }
        return response;
    }

    private static String groupCommand(String[] p, int expectedParts, Supplier<String> operation) {
        return p.length == expectedParts ? operation.get() : WRONG_PARAMETERS;
    }

    private String updateUser(String[] p) {
        if (p.length != 5) {
            return WRONG_PARAMETERS;
        }
        return users.update(p[1], p[2], p[3], p[4]);
    }
}
