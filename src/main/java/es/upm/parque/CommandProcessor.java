package es.upm.parque;

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
    static final String SYSTEM_DATE_SYNTAX = "SYSTEM DATE;yyyy-MM-dd";
    static final String SYSTEM_PRICE_SYNTAX = "SYSTEM PRICE;Precio";
    static final String HELP_SYNTAX = "HELP";
    static final String EXIT_SYNTAX = "EXIT";

    /** Syntax of every supported command, in the order HELP shows them. */
    static final List<String> COMMAND_SYNTAX = List.of(
            USER_CREATE_SYNTAX, USER_DELETE_SYNTAX, USER_UPDATE_SYNTAX,
            GROUP_CREATE_SYNTAX, GROUP_ADD_SYNTAX, GROUP_REMOVE_SYNTAX, GROUP_DELETE_SYNTAX,
            TICKET_CREATE_SYNTAX, TICKET_DELETE_SYNTAX,
            SYSTEM_DATE_SYNTAX, SYSTEM_PRICE_SYNTAX, HELP_SYNTAX, EXIT_SYNTAX);

    private final UserManager userManager;
    private final GroupManager groupManager;
    private final TicketManager ticketManager;
    private final MainController mainController;
    private boolean exitRequested;

    public CommandProcessor(UserManager userManager, GroupManager groupManager, TicketManager ticketManager,
                            MainController mainController) {
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.ticketManager = ticketManager;
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
                userManager.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                yield "OK USER CREATED";
            }
            case "USER DELETE" -> deleteUser(p[1], p[2]);
            case "USER UPDATE" -> {
                userManager.update(p[1], p[2], p[3], p[4]);
                yield "OK USER UPDATED";
            }
            case "GROUP CREATE" -> {
                groupManager.create(p[1], p[2]);
                yield "OK GROUP CREATED";
            }
            case "GROUP ADD" -> {
                groupManager.addMember(p[1], p[2]);
                yield "OK USER ADDED TO GROUP";
            }
            case "GROUP REMOVE" -> {
                groupManager.removeMember(p[1], p[2]);
                yield "OK USER REMOVED FROM GROUP";
            }
            case "GROUP DELETE" -> {
                groupManager.delete(p[1]);
                yield "OK GROUP DELETED";
            }
            case "TICKET CREATE" -> "OK TICKET CREATED -> "
                    + MainController.formatPrice(ticketManager.create(p[1], p[2], p[3]).getPrice());
            case "TICKET DELETE" -> {
                ticketManager.delete(p[1]);
                yield "OK TICKET DELETED";
            }
            case "SYSTEM DATE" -> setSystemDate(p[1]);
            case "SYSTEM PRICE" -> {
                mainController.setTicketPrice(MainController.parsePrice(p[1]));
                yield "OK SYSTEM PRICE SET -> " + MainController.formatPrice(mainController.getTicketPrice());
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
        User user = userManager.authenticate(email, password);
        if (ticketManager.hasCurrentOrFutureTicket(user)) {
            throw new ParkException("User " + email + " has tickets dated on or after the system date");
        }
        userManager.delete(email, password);
        groupManager.removeUserFromAllGroups(user);
        ticketManager.removeTicketsOf(user);
        return "OK USER DELETED";
    }

    private String setSystemDate(String text) {
        mainController.setCurrentDate(MainController.parseDate(text));
        return "OK SYSTEM DATE SET -> " + MainController.formatDate(mainController.getCurrentDate());
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
