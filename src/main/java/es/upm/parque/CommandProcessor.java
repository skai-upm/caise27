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
    static final String ACTIVE_TICKETS = "ERROR USER HAS ACTIVE TICKETS";
    static final String GROUP_LOCKED = "ERROR GROUP LOCKED";
    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;Id",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private final SystemDate systemDate;
    private boolean finished;

    public CommandProcessor(UserManager users, GroupManager groups, TicketManager tickets, SystemDate systemDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
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
                case "GROUP CREATE" -> withParameters(parts, 3, () -> groups.create(parts[1], parts[2]));
                case "GROUP ADD" -> withParameters(parts, 3,
                        () -> unlessLocked(parts[1], () -> groups.addMember(parts[1], parts[2])));
                case "GROUP REMOVE" -> withParameters(parts, 3,
                        () -> unlessLocked(parts[1], () -> groups.removeMember(parts[1], parts[2])));
                case "GROUP DELETE" -> withParameters(parts, 2,
                        () -> unlessLocked(parts[1], () -> groups.delete(parts[1])));
                case "TICKET CREATE" -> withParameters(parts, 4, () -> tickets.create(parts[1], parts[2], parts[3]));
                case "TICKET DELETE" -> withParameters(parts, 2, () -> tickets.delete(parts[1]));
                case "GROUPTICKET CREATE" -> withParameters(parts, 4,
                        () -> tickets.createGroupTicket(parts[1], parts[2], parts[3]));
                case "GROUPTICKET DELETE" -> withParameters(parts, 2, () -> tickets.deleteGroupTicket(parts[1]));
                case "SYSTEM DATE" -> setDate(parts);
                case "SYSTEM PRICE" -> withParameters(parts, 2, () -> tickets.setPrice(parts[1]));
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
        User user = users.checkCredentials(p[1], p[2]);
        if (user != null && tickets.hasActiveTickets(user)) {
            return ACTIVE_TICKETS;
        }
        String response = users.delete(p[1], p[2]);
        if (UserManager.USER_DELETED.equals(response)) {
            groups.removeUserFromAll(user);
        }
        return response;
    }

    /**
     * Runs a group modification unless the group has a current or future group ticket.
     */
    private String unlessLocked(String groupId, Supplier<String> operation) {
        Group group = groups.find(groupId);
        if (group != null && tickets.isGroupLocked(group)) {
            return GROUP_LOCKED;
        }
        return operation.get();
    }

    private static String withParameters(String[] p, int expectedParts, Supplier<String> operation) {
        return p.length == expectedParts ? operation.get() : WRONG_PARAMETERS;
    }

    private String updateUser(String[] p) {
        if (p.length != 5) {
            return WRONG_PARAMETERS;
        }
        return users.update(p[1], p[2], p[3], p[4]);
    }
}
