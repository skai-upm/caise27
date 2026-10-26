package es.upm.parque;

import java.util.List;

/**
 * Interprets one text command, executes it and returns the response line.
 * Commands have the form {@code OPERATION;param1;param2;...}.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private static final String USER_CREATE_SYNTAX = "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta";
    private static final String USER_DELETE_SYNTAX = "USER DELETE;Correo;Contraseña";
    private static final String USER_UPDATE_SYNTAX = "USER UPDATE;Correo;Contraseña;Campo;NuevoValor";
    private static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;yyyy-MM-dd";
    private static final String GROUP_CREATE_SYNTAX = "GROUP CREATE;IdGrupo;Nombre";
    private static final String GROUP_ADD_SYNTAX = "GROUP ADD;IdGrupo;Correo";
    private static final String GROUP_REMOVE_SYNTAX = "GROUP REMOVE;IdGrupo;Correo";
    private static final String GROUP_DELETE_SYNTAX = "GROUP DELETE;IdGrupo";
    private static final String TICKET_CREATE_SYNTAX = "TICKET CREATE;IdEntrada;Correo;yyyy-MM-dd";
    private static final String TICKET_DELETE_SYNTAX = "TICKET DELETE;IdEntrada";
    private static final String GROUPTICKET_CREATE_SYNTAX = "GROUPTICKET CREATE;IdEntradaGrupo;IdGrupo;yyyy-MM-dd";
    private static final String GROUPTICKET_DELETE_SYNTAX = "GROUPTICKET DELETE;IdEntradaGrupo";
    private static final String SYSTEM_PRICE_SYNTAX = "SYSTEM PRICE;Precio";

    /** Syntax of every available command, shown by HELP. */
    private static final List<String> COMMAND_SYNTAX = List.of(
            USER_CREATE_SYNTAX,
            USER_DELETE_SYNTAX,
            USER_UPDATE_SYNTAX,
            GROUP_CREATE_SYNTAX,
            GROUP_ADD_SYNTAX,
            GROUP_REMOVE_SYNTAX,
            GROUP_DELETE_SYNTAX,
            TICKET_CREATE_SYNTAX,
            TICKET_DELETE_SYNTAX,
            GROUPTICKET_CREATE_SYNTAX,
            GROUPTICKET_DELETE_SYNTAX,
            SYSTEM_DATE_SYNTAX,
            SYSTEM_PRICE_SYNTAX,
            "HELP",
            "EXIT");

    private final UserManager userManager;
    private final GroupManager groupManager;
    private final TicketManager ticketManager;
    private final SystemDate systemDate;
    private boolean exitRequested;

    public CommandProcessor(UserManager userManager, GroupManager groupManager, TicketManager ticketManager,
                            SystemDate systemDate) {
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.ticketManager = ticketManager;
        this.systemDate = systemDate;
    }

    public String process(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            return switch (operation) {
                case "USER CREATE" -> createUser(parts);
                case "USER DELETE" -> deleteUser(parts);
                case "USER UPDATE" -> updateUser(parts);
                case "GROUP CREATE" -> createGroup(parts);
                case "GROUP ADD" -> addToGroup(parts);
                case "GROUP REMOVE" -> removeFromGroup(parts);
                case "GROUP DELETE" -> deleteGroup(parts);
                case "TICKET CREATE" -> createTicket(parts);
                case "TICKET DELETE" -> deleteTicket(parts);
                case "GROUPTICKET CREATE" -> createGroupTicket(parts);
                case "GROUPTICKET DELETE" -> deleteGroupTicket(parts);
                case "SYSTEM DATE" -> setSystemDate(parts);
                case "SYSTEM PRICE" -> setSystemPrice(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> "ERROR Unknown command: " + operation;
            };
        } catch (UserException | IllegalArgumentException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != 8) {
            return usage(USER_CREATE_SYNTAX);
        }
        userManager.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "OK USER CREATED";
    }

    private String deleteUser(String[] parts) {
        if (parts.length != 3) {
            return usage(USER_DELETE_SYNTAX);
        }
        User user = userManager.authenticate(parts[1], parts[2]);
        if (ticketManager.hasCurrentOrFutureTicket(user)) {
            return "ERROR User " + parts[1] + " has current or future tickets and cannot be deleted";
        }
        userManager.delete(user);
        groupManager.removeFromAllGroups(user);
        return "OK USER DELETED";
    }

    private String updateUser(String[] parts) {
        if (parts.length != 5) {
            return usage(USER_UPDATE_SYNTAX);
        }
        userManager.update(parts[1], parts[2], parts[3], parts[4]);
        return "OK USER UPDATED";
    }

    private String createGroup(String[] parts) {
        if (parts.length != 3) {
            return usage(GROUP_CREATE_SYNTAX);
        }
        groupManager.create(parts[1], parts[2]);
        return "OK GROUP CREATED";
    }

    private String addToGroup(String[] parts) {
        if (parts.length != 3) {
            return usage(GROUP_ADD_SYNTAX);
        }
        requireUnlocked(parts[1]);
        groupManager.addMember(parts[1], parts[2]);
        return "OK USER ADDED TO GROUP";
    }

    private String removeFromGroup(String[] parts) {
        if (parts.length != 3) {
            return usage(GROUP_REMOVE_SYNTAX);
        }
        requireUnlocked(parts[1]);
        groupManager.removeMember(parts[1], parts[2]);
        return "OK USER REMOVED FROM GROUP";
    }

    private String deleteGroup(String[] parts) {
        if (parts.length != 2) {
            return usage(GROUP_DELETE_SYNTAX);
        }
        requireUnlocked(parts[1]);
        groupManager.delete(parts[1]);
        return "OK GROUP DELETED";
    }

    private void requireUnlocked(String groupId) {
        Group group = groupManager.find(groupId);
        if (group != null && ticketManager.isGroupLocked(group)) {
            throw new UserException("Group " + groupId + " has current or future group tickets and cannot be modified");
        }
    }

    private String createGroupTicket(String[] parts) {
        if (parts.length != 4) {
            return usage(GROUPTICKET_CREATE_SYNTAX);
        }
        GroupTicket ticket = ticketManager.createGroupTicket(parts[1], parts[2], parts[3]);
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String deleteGroupTicket(String[] parts) {
        if (parts.length != 2) {
            return usage(GROUPTICKET_DELETE_SYNTAX);
        }
        ticketManager.deleteGroupTicket(parts[1]);
        return "OK GROUP TICKET DELETED";
    }

    private String createTicket(String[] parts) {
        if (parts.length != 4) {
            return usage(TICKET_CREATE_SYNTAX);
        }
        IndividualTicket ticket = ticketManager.create(parts[1], parts[2], parts[3]);
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String deleteTicket(String[] parts) {
        if (parts.length != 2) {
            return usage(TICKET_DELETE_SYNTAX);
        }
        ticketManager.delete(parts[1]);
        return "OK TICKET DELETED";
    }

    private String setSystemPrice(String[] parts) {
        if (parts.length != 2) {
            return usage(SYSTEM_PRICE_SYNTAX);
        }
        ticketManager.setCurrentPrice(parts[1]);
        return "OK SYSTEM PRICE " + ticketManager.getCurrentPrice().toPlainString();
    }

    private String setSystemDate(String[] parts) {
        if (parts.length != 2) {
            return usage(SYSTEM_DATE_SYNTAX);
        }
        systemDate.set(SystemDate.parse(parts[1].trim()));
        return "OK SYSTEM DATE " + SystemDate.format(systemDate.get());
    }

    private String help(String[] parts) {
        if (parts.length != 1) {
            return usage("HELP");
        }
        return "Available commands:" + System.lineSeparator()
                + String.join(System.lineSeparator(), COMMAND_SYNTAX);
    }

    private String exit(String[] parts) {
        if (parts.length != 1) {
            return usage("EXIT");
        }
        exitRequested = true;
        return "OK EXIT";
    }

    /**
     * Tells whether EXIT has been processed, so no further commands must be executed.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private static String usage(String syntax) {
        return "ERROR Wrong number of parameters. Usage: " + syntax;
    }
}
