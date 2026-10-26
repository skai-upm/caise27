package es.upm.parque.presentation;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.controller.AttractionController;
import es.upm.parque.business.controller.GroupController;
import es.upm.parque.business.controller.InvoiceController;
import es.upm.parque.business.controller.MainController;
import es.upm.parque.business.controller.TicketController;
import es.upm.parque.business.controller.UserController;
import es.upm.parque.business.invoice.Invoice;
import es.upm.parque.business.invoice.InvoiceLine;
import es.upm.parque.business.invoice.InvoiceSection;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.Values;
import es.upm.parque.presentation.help.CommandInfo;
import es.upm.parque.presentation.help.HelpGenerator;
import java.util.List;
import java.util.Map;

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
    static final String INVOICE_CREATE_SYNTAX = "INVOICE CREATE;USER|GROUP;Correo|IdentificadorGrupo";
    static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;yyyy-MM-dd";
    static final String SYSTEM_PRICE_SYNTAX = "SYSTEM PRICE;Precio";
    static final String HELP_SYNTAX = "HELP";
    static final String HELP_EXTENDED_SYNTAX = "HELP EXTENDED";
    static final String EXIT_SYNTAX = "EXIT";

    /** Registry of every supported command, in the order the help shows them. */
    public static final List<CommandInfo> COMMANDS = List.of(
            new CommandInfo(USER_CREATE_SYNTAX,
                    "Registers a user after validating every field; DNI and email must be unique."),
            new CommandInfo(USER_DELETE_SYNTAX,
                    "Deletes the user after checking the password, unless they have tickets dated today or later."),
            new CommandInfo(USER_UPDATE_SYNTAX,
                    "Changes one field (NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD) after checking the password; the DNI cannot change."),
            new CommandInfo(GROUP_CREATE_SYNTAX,
                    "Creates an empty group with a unique identifier."),
            new CommandInfo(GROUP_ADD_SYNTAX,
                    "Adds a registered user to a group that is not locked by a current or future group ticket."),
            new CommandInfo(GROUP_REMOVE_SYNTAX,
                    "Removes a member from a group that is not locked by a current or future group ticket."),
            new CommandInfo(GROUP_DELETE_SYNTAX,
                    "Deletes an unlocked group; its users stay registered."),
            new CommandInfo(TICKET_CREATE_SYNTAX,
                    "Buys an individual ticket for a user and date (not before the system date) at the current price."),
            new CommandInfo(TICKET_DELETE_SYNTAX,
                    "Cancels a standalone individual ticket dated after the system date."),
            new CommandInfo(GROUPTICKET_CREATE_SYNTAX,
                    "Buys one ticket per current group member with a size-based discount (0/10/20/50%)."),
            new CommandInfo(GROUPTICKET_DELETE_SYNTAX,
                    "Cancels a group ticket dated after the system date together with its member tickets."),
            new CommandInfo(ATTRACTION_CREATE_SYNTAX,
                    "Creates an attraction with a positive maximum capacity."),
            new CommandInfo(ATTRACTION_ENTER_SYNTAX,
                    "Lets the users of a ticket valid today into an attraction; all of them or none, within capacity."),
            new CommandInfo(ATTRACTION_EXIT_SYNTAX,
                    "Lets the users of a ticket out of an attraction, freeing their places."),
            new CommandInfo(ATTRACTION_STATUS_SYNTAX,
                    "Shows capacity, occupancy, available places and the users present in an attraction."),
            new CommandInfo(INVOICE_CREATE_SYNTAX,
                    "Shows the invoice of a user (direct tickets) or a group (group tickets) with prices and total."),
            new CommandInfo(SYSTEM_DATE_SYNTAX,
                    "Sets the simulated current date used by every date rule."),
            new CommandInfo(SYSTEM_PRICE_SYNTAX,
                    "Sets the current individual ticket price (40.00 at every start)."),
            new CommandInfo(HELP_SYNTAX,
                    "Lists the syntax of every command."),
            new CommandInfo(HELP_EXTENDED_SYNTAX,
                    "Lists every command with its syntax and a short description."),
            new CommandInfo(EXIT_SYNTAX,
                    "Ends the application; in a file, no later line is executed."));

    private final UserController userController;
    private final GroupController groupController;
    private final TicketController ticketController;
    private final AttractionController attractionController;
    private final InvoiceController invoiceController;
    private final MainController mainController;
    private final Map<String, HelpGenerator> helpGenerators;
    private boolean exitRequested;

    public CommandProcessor(UserController userController, GroupController groupController, TicketController ticketController,
                            AttractionController attractionController, InvoiceController invoiceController,
                            MainController mainController, Map<String, HelpGenerator> helpGenerators) {
        this.userController = userController;
        this.groupController = groupController;
        this.ticketController = ticketController;
        this.attractionController = attractionController;
        this.invoiceController = invoiceController;
        this.mainController = mainController;
        this.helpGenerators = Map.copyOf(helpGenerators);
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
        CommandInfo command = commandOf(operation);
        if (command == null) {
            return "ERROR Unknown command: " + parts[0];
        }
        try {
            requireParams(parts, command);
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
            case "INVOICE CREATE" -> createInvoice(p[1], p[2]);
            case "SYSTEM DATE" -> setSystemDate(p[1]);
            case "SYSTEM PRICE" -> {
                mainController.setTicketPrice(Values.parsePrice(p[1]));
                yield "OK SYSTEM PRICE SET -> " + Values.formatPrice(mainController.getTicketPrice());
            }
            case "HELP" -> "OK Available commands:" + System.lineSeparator() + helpGenerators.get(operation).generate();
            case "HELP EXTENDED" -> "OK Available commands (extended):" + System.lineSeparator()
                    + helpGenerators.get(operation).generate();
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

    private String createInvoice(String type, String id) {
        String kind = type.toUpperCase();
        Invoice invoice = switch (kind) {
            case "USER" -> invoiceController.forUser(id);
            case "GROUP" -> invoiceController.forGroup(id);
            default -> throw new ParkException("Invoice recipient type must be USER or GROUP: " + type);
        };
        String nl = System.lineSeparator();
        StringBuilder out = new StringBuilder("OK INVOICE " + kind + " " + id)
                .append(nl).append("Recipient: ").append(invoice.getRecipient());
        for (InvoiceSection section : invoice.getSections()) {
            out.append(nl).append(section.title()).append(':');
            for (InvoiceLine line : section.lines()) {
                out.append(nl).append("- ").append(line.description()).append(' ').append(Values.formatPrice(line.amount()));
            }
        }
        return out.append(nl).append("Total: ").append(Values.formatPrice(invoice.getTotal())).toString();
    }

    private String setSystemDate(String text) {
        mainController.setCurrentDate(Values.parseDate(text));
        return "OK SYSTEM DATE SET -> " + Values.formatDate(mainController.getCurrentDate());
    }

    /**
     * Returns the registered command whose operation matches, or {@code null} if unknown.
     */
    private static CommandInfo commandOf(String operation) {
        return COMMANDS.stream().filter(c -> c.operation().equals(operation)).findFirst().orElse(null);
    }

    /**
     * Checks that the command has as many parameters as its syntax declares.
     */
    private static void requireParams(String[] parts, CommandInfo command) {
        int expected = command.parameterCount();
        if (parts.length - 1 != expected) {
            throw new ParkException("Wrong number of parameters (expected " + expected + ", got "
                    + (parts.length - 1) + "). Usage: " + command.syntax());
        }
    }
}
