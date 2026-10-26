package es.upm.parque.presentation;

import es.upm.parque.business.BusinessException;
import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.SystemDate;
import es.upm.parque.business.model.User;

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
    private static final String ATTRACTION_CREATE_SYNTAX = "ATTRACTION CREATE;IdAtraccion;Nombre;Capacidad";
    private static final String ATTRACTION_DELETE_SYNTAX = "ATTRACTION DELETE;IdAtraccion";
    private static final String ATTRACTION_ENTER_SYNTAX = "ATTRACTION ENTER;IdAtraccion;IdEntrada";
    private static final String ATTRACTION_EXIT_SYNTAX = "ATTRACTION EXIT;IdAtraccion;IdEntrada";
    private static final String ATTRACTION_STATUS_SYNTAX = "ATTRACTION STATUS;IdAtraccion";
    private static final String SYSTEM_PRICE_SYNTAX = "SYSTEM PRICE;Precio";
    private static final String INVOICE_CREATE_SYNTAX = "INVOICE CREATE;USER|GROUP;Correo|IdGrupo";

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
            ATTRACTION_CREATE_SYNTAX,
            ATTRACTION_DELETE_SYNTAX,
            ATTRACTION_ENTER_SYNTAX,
            ATTRACTION_EXIT_SYNTAX,
            ATTRACTION_STATUS_SYNTAX,
            INVOICE_CREATE_SYNTAX,
            SYSTEM_DATE_SYNTAX,
            SYSTEM_PRICE_SYNTAX,
            "HELP",
            "EXIT");

    private final UserController userController;
    private final GroupController groupController;
    private final TicketController ticketController;
    private final AttractionController attractionController;
    private final InvoiceController invoiceController;
    private final SystemDate systemDate;
    private boolean exitRequested;

    public CommandProcessor(UserController userController, GroupController groupController, TicketController ticketController,
                            AttractionController attractionController, InvoiceController invoiceController,
                            SystemDate systemDate) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.attractionController = attractionController;
        this.invoiceController = invoiceController;
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
                case "ATTRACTION CREATE" -> createAttraction(parts);
                case "ATTRACTION DELETE" -> deleteAttraction(parts);
                case "ATTRACTION ENTER" -> enterAttraction(parts);
                case "ATTRACTION EXIT" -> exitAttraction(parts);
                case "ATTRACTION STATUS" -> attractionStatus(parts);
                case "INVOICE CREATE" -> createInvoice(parts);
                case "SYSTEM DATE" -> setSystemDate(parts);
                case "SYSTEM PRICE" -> setSystemPrice(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> "ERROR Unknown command: " + operation;
            };
        } catch (BusinessException | IllegalArgumentException e) {
            return "ERROR " + e.getMessage();
        }
    }

    private String createUser(String[] parts) {
        if (parts.length != 8) {
            return usage(USER_CREATE_SYNTAX);
        }
        userController.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "OK USER CREATED";
    }

    private String deleteUser(String[] parts) {
        if (parts.length != 3) {
            return usage(USER_DELETE_SYNTAX);
        }
        User user = userController.authenticate(parts[1], parts[2]);
        if (ticketController.hasCurrentOrFutureTicket(user)) {
            return "ERROR User " + parts[1] + " has current or future tickets and cannot be deleted";
        }
        userController.delete(user);
        groupController.removeFromAllGroups(user);
        return "OK USER DELETED";
    }

    private String updateUser(String[] parts) {
        if (parts.length != 5) {
            return usage(USER_UPDATE_SYNTAX);
        }
        userController.update(parts[1], parts[2], parts[3], parts[4]);
        return "OK USER UPDATED";
    }

    private String createGroup(String[] parts) {
        if (parts.length != 3) {
            return usage(GROUP_CREATE_SYNTAX);
        }
        groupController.create(parts[1], parts[2]);
        return "OK GROUP CREATED";
    }

    private String addToGroup(String[] parts) {
        if (parts.length != 3) {
            return usage(GROUP_ADD_SYNTAX);
        }
        requireUnlocked(parts[1]);
        groupController.addMember(parts[1], parts[2]);
        return "OK USER ADDED TO GROUP";
    }

    private String removeFromGroup(String[] parts) {
        if (parts.length != 3) {
            return usage(GROUP_REMOVE_SYNTAX);
        }
        requireUnlocked(parts[1]);
        groupController.removeMember(parts[1], parts[2]);
        return "OK USER REMOVED FROM GROUP";
    }

    private String deleteGroup(String[] parts) {
        if (parts.length != 2) {
            return usage(GROUP_DELETE_SYNTAX);
        }
        requireUnlocked(parts[1]);
        groupController.delete(parts[1]);
        return "OK GROUP DELETED";
    }

    private void requireUnlocked(String groupId) {
        Group group = groupController.find(groupId);
        if (group != null && ticketController.isGroupLocked(group)) {
            throw new BusinessException("Group " + groupId + " has current or future group tickets and cannot be modified");
        }
    }

    private String createGroupTicket(String[] parts) {
        if (parts.length != 4) {
            return usage(GROUPTICKET_CREATE_SYNTAX);
        }
        GroupTicket ticket = ticketController.createGroupTicket(parts[1], parts[2], parts[3]);
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String deleteGroupTicket(String[] parts) {
        if (parts.length != 2) {
            return usage(GROUPTICKET_DELETE_SYNTAX);
        }
        ticketController.deleteGroupTicket(parts[1]);
        return "OK GROUP TICKET DELETED";
    }

    private String createAttraction(String[] parts) {
        if (parts.length != 4) {
            return usage(ATTRACTION_CREATE_SYNTAX);
        }
        attractionController.create(parts[1], parts[2], parts[3]);
        return "OK ATTRACTION CREATED";
    }

    private String deleteAttraction(String[] parts) {
        if (parts.length != 2) {
            return usage(ATTRACTION_DELETE_SYNTAX);
        }
        attractionController.delete(parts[1]);
        return "OK ATTRACTION DELETED";
    }

    private String enterAttraction(String[] parts) {
        if (parts.length != 3) {
            return usage(ATTRACTION_ENTER_SYNTAX);
        }
        int entered = attractionController.enter(parts[1], parts[2]);
        return "OK ATTRACTION ENTERED -> " + entered + " users";
    }

    private String exitAttraction(String[] parts) {
        if (parts.length != 3) {
            return usage(ATTRACTION_EXIT_SYNTAX);
        }
        int left = attractionController.exit(parts[1], parts[2]);
        return "OK ATTRACTION EXITED -> " + left + " users";
    }

    private String attractionStatus(String[] parts) {
        if (parts.length != 2) {
            return usage(ATTRACTION_STATUS_SYNTAX);
        }
        Attraction attraction = attractionController.require(parts[1]);
        List<User> visitors = attraction.getVisitors();
        StringBuilder status = new StringBuilder("OK ATTRACTION STATUS ").append(attraction.getId())
                .append(System.lineSeparator()).append("Name: ").append(attraction.getName())
                .append(System.lineSeparator()).append("Capacity: ").append(attraction.getCapacity())
                .append(System.lineSeparator()).append("Present: ").append(visitors.size())
                .append(System.lineSeparator()).append("Available: ").append(attraction.available())
                .append(System.lineSeparator()).append("Users:");
        if (visitors.isEmpty()) {
            status.append(System.lineSeparator()).append("- none");
        }
        for (User user : visitors) {
            status.append(System.lineSeparator()).append("- ").append(user.getName()).append(' ')
                    .append(user.getSurname()).append(" (").append(user.getDni()).append(')');
        }
        return status.toString();
    }

    private String createInvoice(String[] parts) {
        if (parts.length != 3) {
            return usage(INVOICE_CREATE_SYNTAX);
        }
        Invoice invoice = switch (parts[1].trim()) {
            case "USER" -> invoiceController.userInvoice(parts[2]);
            case "GROUP" -> invoiceController.groupInvoice(parts[2]);
            default -> throw new BusinessException("Invalid invoice recipient type: " + parts[1] + " (USER or GROUP)");
        };
        return "OK INVOICE" + System.lineSeparator() + String.join(System.lineSeparator(), invoice.toLines());
    }

    private String createTicket(String[] parts) {
        if (parts.length != 4) {
            return usage(TICKET_CREATE_SYNTAX);
        }
        IndividualTicket ticket = ticketController.create(parts[1], parts[2], parts[3]);
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String deleteTicket(String[] parts) {
        if (parts.length != 2) {
            return usage(TICKET_DELETE_SYNTAX);
        }
        ticketController.delete(parts[1]);
        return "OK TICKET DELETED";
    }

    private String setSystemPrice(String[] parts) {
        if (parts.length != 2) {
            return usage(SYSTEM_PRICE_SYNTAX);
        }
        ticketController.setCurrentPrice(parts[1]);
        return "OK SYSTEM PRICE " + ticketController.getCurrentPrice().toPlainString();
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
