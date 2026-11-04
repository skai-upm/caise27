package com.parque;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Interprets text commands ({@code OPERATION;param;param...}) using String.split and answers with
 * a response whose first line starts with {@code OK} or {@code ERROR:}. No exception escapes
 * {@link #process(String)}. The same instance serves both interactive and file input.
 * <p>
 * It is the application's main controller and owns the single current system date and the current
 * individual ticket price of the execution.
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
            "TICKET CREATE;Identificador;Correo;yyyy-MM-dd",
            "TICKET DELETE;Identificador",
            "GROUPTICKET CREATE;Identificador;Grupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;Identificador",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    /** Individual ticket price at the start of every execution. */
    static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserService users;
    private final GroupService groups;
    private final TicketService tickets;
    private boolean exitRequested;
    private LocalDate currentDate;
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    /** Creates the controller with the operating system's local date as the current date. */
    public CommandProcessor(UserService users, GroupService groups, TicketService tickets) {
        this(users, groups, tickets, LocalDate.now());
    }

    public CommandProcessor(UserService users, GroupService groups, TicketService tickets, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.currentDate = initialDate;
    }

    /** The single current date of the execution; every date-dependent rule must use it. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /** Price applied to individual tickets created from now on. */
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    /** True once an EXIT command has been processed; no further commands must be executed. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    public String process(String line) {
        try {
            return execute(line);
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
        switch (operation) {
            case "HELP" -> {
                expectParameters(parts, 0, "HELP");
                return ok(help());
            }
            case "EXIT" -> {
                expectParameters(parts, 0, "EXIT");
                exitRequested = true;
                return ok("Exiting");
            }
            case "SYSTEM DATE" -> {
                expectParameters(parts, 1, "SYSTEM DATE;yyyy-MM-dd");
                currentDate = parseDate(parts[1]);
                return ok("System date set to " + currentDate);
            }
            case "SYSTEM PRICE" -> {
                expectParameters(parts, 1, "SYSTEM PRICE;Precio");
                ticketPrice = parsePrice(parts[1]);
                return ok("Ticket price set to " + ticketPrice.toPlainString());
            }
            case "USER CREATE" -> {
                expectParameters(parts, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                users.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return ok("User created");
            }
            case "USER DELETE" -> {
                expectParameters(parts, 2, "USER DELETE;Correo;Contraseña");
                User user = users.authenticate(parts[1], parts[2]);
                if (tickets.hasCurrentOrFutureTicket(user, currentDate)) {
                    throw new IllegalArgumentException("User " + parts[1]
                            + " has tickets dated on or after " + currentDate + " and cannot be deleted");
                }
                User deleted = users.delete(parts[1], parts[2]);
                groups.removeUserFromAllGroups(deleted);
                return ok("User deleted");
            }
            case "USER UPDATE" -> {
                expectParameters(parts, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                UserField field = UserField.fromName(parts[3]);
                if (field == null) {
                    throw new IllegalArgumentException("Field cannot be updated: " + parts[3]
                            + " (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                }
                users.update(parts[1], parts[2], field, parts[4]);
                return ok("User updated");
            }
            case "GROUP CREATE" -> {
                expectParameters(parts, 2, "GROUP CREATE;Identificador;Nombre");
                groups.create(parts[1], parts[2]);
                return ok("Group created");
            }
            case "GROUP ADD" -> {
                expectParameters(parts, 2, "GROUP ADD;Identificador;Correo");
                requireGroupUnlocked(parts[1]);
                groups.addMember(parts[1], parts[2]);
                return ok("User added to group");
            }
            case "GROUP REMOVE" -> {
                expectParameters(parts, 2, "GROUP REMOVE;Identificador;Correo");
                requireGroupUnlocked(parts[1]);
                groups.removeMember(parts[1], parts[2]);
                return ok("User removed from group");
            }
            case "GROUP DELETE" -> {
                expectParameters(parts, 1, "GROUP DELETE;Identificador");
                requireGroupUnlocked(parts[1]);
                groups.delete(parts[1]);
                return ok("Group deleted");
            }
            case "TICKET CREATE" -> {
                expectParameters(parts, 3, "TICKET CREATE;Identificador;Correo;yyyy-MM-dd");
                IndividualTicket ticket = tickets.create(parts[1], parts[2], parseDate(parts[3]), currentDate, ticketPrice);
                return "OK TICKET CREATED -> " + ticket.getPriceText();
            }
            case "TICKET DELETE" -> {
                expectParameters(parts, 1, "TICKET DELETE;Identificador");
                tickets.delete(parts[1], currentDate);
                return ok("Ticket deleted");
            }
            case "GROUPTICKET CREATE" -> {
                expectParameters(parts, 3, "GROUPTICKET CREATE;Identificador;Grupo;yyyy-MM-dd");
                GroupTicket ticket = tickets.createGroupTicket(parts[1], parts[2], parseDate(parts[3]), currentDate,
                        ticketPrice);
                return "OK GROUP TICKET CREATED -> " + ticket.getPriceText();
            }
            case "GROUPTICKET DELETE" -> {
                expectParameters(parts, 1, "GROUPTICKET DELETE;Identificador");
                tickets.deleteGroupTicket(parts[1], currentDate);
                return ok("Group ticket deleted");
            }
            default -> throw new IllegalArgumentException("Unknown command: " + operation);
        }
    }

    /** Rejects changes to a group that has a group ticket dated on or after the system date. */
    private void requireGroupUnlocked(String groupId) {
        Group group = groups.find(groupId);
        if (group != null && tickets.isGroupLocked(group, currentDate)) {
            throw new IllegalArgumentException("Group " + groupId + " has group tickets dated on or after "
                    + currentDate + " and cannot be modified or deleted");
        }
    }

    private static String ok(String message) {
        return "OK: " + message;
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

    /** Positive amount: digits, optionally '.' and one or two decimals. Returned with scale 2. */
    static BigDecimal parsePrice(String value) {
        int dot = value.indexOf('.');
        String whole = dot < 0 ? value : value.substring(0, dot);
        String decimals = dot < 0 ? "" : value.substring(dot + 1);
        boolean valid = !whole.isEmpty() && allDigits(whole) && allDigits(decimals)
                && (dot < 0 || (decimals.length() >= 1 && decimals.length() <= 2));
        if (!valid) {
            throw new IllegalArgumentException("Invalid price: '" + value + "' (expected a positive amount such as 60.00)");
        }
        BigDecimal price = new BigDecimal(value).setScale(2);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException("Invalid price: '" + value + "' (it must be greater than zero)");
        }
        return price;
    }

    private static boolean allDigits(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) < '0' || s.charAt(i) > '9') {
                return false;
            }
        }
        return true;
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
