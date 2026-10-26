package es.upm.parque.presentation;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.Values;
import java.util.List;

/**
 * Interprets one text command, split on ';', and returns the response text.
 * Interactive and file modes both use this class, so every command behaves the same in both.
 */
public class CommandProcessor {

    static final String USER_CREATE_SYNTAX = "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta";
    static final String USER_DELETE_SYNTAX = "USER DELETE;Correo;Contraseña";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;Correo;Contraseña;Campo;NuevoValor";
    static final String GROUP_CREATE_SYNTAX = "GROUP CREATE;IdentificadorGrupo;Nombre";
    static final String GROUP_ADD_SYNTAX = "GROUP ADD;IdentificadorGrupo;Correo";
    static final String GROUP_REMOVE_SYNTAX = "GROUP REMOVE;IdentificadorGrupo;Correo";
    static final String GROUP_DELETE_SYNTAX = "GROUP DELETE;IdentificadorGrupo";
    static final String TICKET_CREATE_SYNTAX = "TICKET CREATE;IdentificadorEntrada;Correo;yyyy-MM-dd";
    static final String TICKET_DELETE_SYNTAX = "TICKET DELETE;IdentificadorEntrada";
    static final String GROUPTICKET_CREATE_SYNTAX = "GROUPTICKET CREATE;IdentificadorEntradaGrupo;IdentificadorGrupo;yyyy-MM-dd";
    static final String GROUPTICKET_DELETE_SYNTAX = "GROUPTICKET DELETE;IdentificadorEntradaGrupo";
    static final String ATTRACTION_CREATE_SYNTAX = "ATTRACTION CREATE;IdentificadorAtraccion;Nombre;Capacidad";
    static final String ATTRACTION_ENTER_SYNTAX = "ATTRACTION ENTER;IdentificadorAtraccion;IdentificadorEntrada";
    static final String ATTRACTION_EXIT_SYNTAX = "ATTRACTION EXIT;IdentificadorAtraccion;IdentificadorEntrada";
    static final String ATTRACTION_STATUS_SYNTAX = "ATTRACTION STATUS;IdentificadorAtraccion";
    static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;yyyy-MM-dd";
    static final String SYSTEM_PRICE_SYNTAX = "SYSTEM PRICE;Precio";
    static final String HELP_SYNTAX = "HELP";
    static final String EXIT_SYNTAX = "EXIT";

    /** Syntax of every supported command, in the order HELP shows them. */
    static final List<String> COMMAND_SYNTAX = List.of(
            USER_CREATE_SYNTAX, USER_DELETE_SYNTAX, USER_UPDATE_SYNTAX,
            GROUP_CREATE_SYNTAX, GROUP_ADD_SYNTAX, GROUP_REMOVE_SYNTAX, GROUP_DELETE_SYNTAX,
            TICKET_CREATE_SYNTAX, TICKET_DELETE_SYNTAX, GROUPTICKET_CREATE_SYNTAX, GROUPTICKET_DELETE_SYNTAX,
            ATTRACTION_CREATE_SYNTAX, ATTRACTION_ENTER_SYNTAX, ATTRACTION_EXIT_SYNTAX, ATTRACTION_STATUS_SYNTAX,
            SYSTEM_DATE_SYNTAX, SYSTEM_PRICE_SYNTAX, HELP_SYNTAX, EXIT_SYNTAX);

    private final UserController userController;
    private final GroupController groupController;
    private final TicketController ticketController;
    private final AttractionController attractionController;
    private final MainController mainController;
    private boolean exitRequested;

    public CommandProcessor(UserController userController, GroupController groupController, TicketController ticketController,
                            AttractionController attractionController, MainController mainController) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.attractionController = attractionController;
        this.mainController = mainController;
    }

    /**
     * True once an EXIT command has been processed.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    public String process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String operation = parts[0].toUpperCase();
        String syntax = syntaxOf(operation);
        if (syntax == null) {
            return "ERROR Unknown command: " + parts[0];
        }
        try {
            requireParams(parts, syntax);
            return execute(operation, parts);
        } catch (ParkException e) {
            return "ERROR " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR Unexpected error: " + e.getMessage();
        }
    }

    private String execute(String operation, String[] p) {
        return switch (operation) {
            case "USER CREATE" -> {
                userController.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                yield "OK USER CREATED";
            }
            case "USER DELETE" -> deleteUser(p[1], p[2]);
            case "USER UPDATE" -> {
                userController.update(p[1], p[2], p[3], p[4]);
                yield "OK USER UPDATED";
            }
            case "GROUP CREATE" -> {
                groupController.create(p[1], p[2]);
                yield "OK GROUP CREATED";
            }
            case "GROUP ADD" -> {
                requireUnlocked(p[1]);
                groupController.addMember(p[1], p[2]);
                yield "OK USER ADDED TO GROUP";
            }
            case "GROUP REMOVE" -> {
                requireUnlocked(p[1]);
                groupController.removeMember(p[1], p[2]);
                yield "OK USER REMOVED FROM GROUP";
            }
            case "GROUP DELETE" -> deleteGroup(p[1]);
            case "TICKET CREATE" -> "OK TICKET CREATED -> "
                    + Values.formatPrice(ticketController.create(p[1], p[2], p[3]).getPrice());
            case "TICKET DELETE" -> {
                ticketController.delete(p[1]);
                yield "OK TICKET DELETED";
            }
            case "GROUPTICKET CREATE" -> "OK GROUP TICKET CREATED -> "
                    + Values.formatPrice(ticketController.createGroupTicket(p[1], p[2], p[3]).getPrice());
            case "GROUPTICKET DELETE" -> {
                ticketController.deleteGroupTicket(p[1]);
                yield "OK GROUP TICKET DELETED";
            }
            case "ATTRACTION CREATE" -> {
                attractionController.create(p[1], p[2], p[3]);
                yield "OK ATTRACTION CREATED";
            }
            case "ATTRACTION ENTER" -> "OK ENTERED " + p[1] + " -> " + users(attractionController.enter(p[1], p[2]));
            case "ATTRACTION EXIT" -> "OK EXITED " + p[1] + " -> " + users(attractionController.exit(p[1], p[2]));
            case "ATTRACTION STATUS" -> attractionStatus(attractionController.requireAttraction(p[1]));
            case "SYSTEM DATE" -> setSystemDate(p[1]);
            case "SYSTEM PRICE" -> {
                mainController.setTicketPrice(Values.parsePrice(p[1]));
                yield "OK SYSTEM PRICE SET -> " + Values.formatPrice(mainController.getTicketPrice());
            }
            case "HELP" -> "OK Available commands:" + System.lineSeparator()
                    + String.join(System.lineSeparator(), COMMAND_SYNTAX);
            case "EXIT" -> {
                exitRequested = true;
                yield "OK EXIT";
            }
            default -> throw new IllegalStateException("No handler for " + operation);
        };
    }

    private String deleteUser(String email, String password) {
        User user = userController.authenticate(email, password);
        if (ticketController.hasCurrentOrFutureTicket(user)) {
            throw new ParkException("User " + email + " has tickets dated on or after the system date");
        }
        userController.delete(email, password);
        groupController.removeUserFromAllGroups(user);
        ticketController.removeTicketsOf(user);
        return "OK USER DELETED";
    }

    private String deleteGroup(String id) {
        requireUnlocked(id);
        Group group = groupController.findById(id);
        groupController.delete(id);
        ticketController.removeGroupTicketsOf(group);
        return "OK GROUP DELETED";
    }

    /**
     * Rejects changes to a group that has a group ticket dated on or after the system date.
     */
    private void requireUnlocked(String groupId) {
        Group group = groupController.findById(groupId);
        if (group != null && ticketController.isGroupLocked(group)) {
            throw new ParkException("Group " + groupId + " is locked by a group ticket dated on or after the system date");
        }
    }

    private static String users(int count) {
        return count + (count == 1 ? " user" : " users");
    }

    private static String attractionStatus(Attraction a) {
        StringBuilder out = new StringBuilder("OK ATTRACTION STATUS " + a.getId());
        String nl = System.lineSeparator();
        out.append(nl).append("Name: ").append(a.getName())
                .append(nl).append("Capacity: ").append(a.getCapacity())
                .append(nl).append("Present: ").append(a.getOccupancy())
                .append(nl).append("Available: ").append(a.getAvailablePlaces())
                .append(nl).append("Users present:");
        if (a.getVisitors().isEmpty()) {
            out.append(nl).append("- none");
        }
        for (User u : a.getVisitors()) {
            out.append(nl).append("- ").append(u.getName()).append(' ').append(u.getSurname())
                    .append(" (").append(u.getEmail()).append(')');
        }
        return out.toString();
    }

    private String setSystemDate(String text) {
        mainController.setCurrentDate(Values.parseDate(text));
        return "OK SYSTEM DATE SET -> " + Values.formatDate(mainController.getCurrentDate());
    }

    /**
     * Returns the syntax of the command whose operation matches, or {@code null} if unknown.
     */
    private static String syntaxOf(String operation) {
        for (String syntax : COMMAND_SYNTAX) {
            if (syntax.split(";", -1)[0].equals(operation)) {
                return syntax;
            }
        }
        return null;
    }

    /**
     * Checks that the command has as many parameters as its syntax declares.
     */
    private static void requireParams(String[] parts, String syntax) {
        int expected = syntax.split(";", -1).length - 1;
        if (parts.length - 1 != expected) {
            throw new ParkException("Wrong number of parameters (expected " + expected + ", got "
                    + (parts.length - 1) + "). Usage: " + syntax);
        }
    }
}
