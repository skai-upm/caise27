package es.upm.parque;

import java.math.BigDecimal;
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
        "TICKET CREATE;Id;Correo;yyyy-MM-dd",
        "TICKET DELETE;Id",
        "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
        "GROUPTICKET DELETE;Id",
        "ATTRACTION CREATE;Id;Nombre;Capacidad",
        "ATTRACTION DELETE;Id",
        "ATTRACTION ENTER;IdAtraccion;IdEntrada",
        "ATTRACTION EXIT;IdAtraccion;IdEntrada",
        "ATTRACTION STATUS;IdAtraccion",
        "SYSTEM DATE;yyyy-MM-dd",
        "SYSTEM PRICE;Importe",
        "HELP",
        "EXIT"
    };

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserManager userManager;
    private final GroupManager groupManager;
    private final TicketManager ticketManager;
    private final AttractionManager attractionManager;
    private final SystemDate systemDate;
    private boolean exitRequested;

    public CommandProcessor(UserManager userManager, GroupManager groupManager, TicketManager ticketManager,
            AttractionManager attractionManager, SystemDate systemDate) {
        this.userManager = userManager;
        this.groupManager = groupManager;
        this.ticketManager = ticketManager;
        this.attractionManager = attractionManager;
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
            if (operation.length == 2 && operation[0].equals("TICKET")) {
                return processTicket(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("GROUPTICKET")) {
                return processGroupTicket(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("ATTRACTION")) {
                return processAttraction(operation[1], parts);
            }
            if (operation.length == 2 && operation[0].equals("SYSTEM") && operation[1].equals("DATE")) {
                requireParams(parts, 1, "SYSTEM DATE;yyyy-MM-dd");
                systemDate.set(parseDate(parts[1]));
                return "OK SYSTEM DATE -> " + systemDate.get();
            }
            if (operation.length == 2 && operation[0].equals("SYSTEM") && operation[1].equals("PRICE")) {
                requireParams(parts, 1, "SYSTEM PRICE;Importe");
                ticketManager.setPrice(parsePrice(parts[1]));
                return "OK SYSTEM PRICE -> " + ticketManager.getPrice().toPlainString();
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

    private String processTicket(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 3, "TICKET CREATE;Id;Correo;yyyy-MM-dd");
                IndividualTicket t = ticketManager.create(p[1], p[2], parseDate(p[3]));
                return "OK TICKET CREATED -> " + t.getPrice().toPlainString();
            }
            case "DELETE" -> {
                requireParams(p, 1, "TICKET DELETE;Id");
                return "OK TICKET DELETED -> " + ticketManager.delete(p[1]).getId();
            }
            default -> {
                return "ERROR: unknown command 'TICKET " + action + "'";
            }
        }
    }

    private String processGroupTicket(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 3, "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
                GroupTicket t = ticketManager.createGroupTicket(p[1], p[2], parseDate(p[3]));
                return "OK GROUP TICKET CREATED -> " + t.getPrice().toPlainString();
            }
            case "DELETE" -> {
                requireParams(p, 1, "GROUPTICKET DELETE;Id");
                return "OK GROUP TICKET DELETED -> " + ticketManager.deleteGroupTicket(p[1]).getId();
            }
            default -> {
                return "ERROR: unknown command 'GROUPTICKET " + action + "'";
            }
        }
    }

    private String processAttraction(String action, String[] p) {
        switch (action) {
            case "CREATE" -> {
                requireParams(p, 3, "ATTRACTION CREATE;Id;Nombre;Capacidad");
                return "OK ATTRACTION CREATED -> " + attractionManager.create(p[1], p[2], parseCapacity(p[3]));
            }
            case "DELETE" -> {
                requireParams(p, 1, "ATTRACTION DELETE;Id");
                return "OK ATTRACTION DELETED -> " + attractionManager.delete(p[1]).getId();
            }
            case "ENTER" -> {
                requireParams(p, 2, "ATTRACTION ENTER;IdAtraccion;IdEntrada");
                return "OK ATTRACTION ENTERED -> " + attractionManager.enter(p[1], p[2]);
            }
            case "EXIT" -> {
                requireParams(p, 2, "ATTRACTION EXIT;IdAtraccion;IdEntrada");
                return "OK ATTRACTION EXITED -> " + attractionManager.exit(p[1], p[2]);
            }
            case "STATUS" -> {
                requireParams(p, 1, "ATTRACTION STATUS;IdAtraccion");
                return "OK " + attractionManager.status(p[1]);
            }
            default -> {
                return "ERROR: unknown command 'ATTRACTION " + action + "'";
            }
        }
    }

    private static int parseCapacity(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid capacity '" + text + "': it must be a positive integer");
        }
    }

    private static BigDecimal parsePrice(String text) {
        try {
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid price '" + text + "'");
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
