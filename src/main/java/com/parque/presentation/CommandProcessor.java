package com.parque.presentation;

import com.parque.business.controller.ParkController;
import com.parque.business.model.Attraction;
import com.parque.business.model.GroupTicket;
import com.parque.business.model.IndividualTicket;
import com.parque.business.model.User;
import com.parque.business.model.UserField;
import com.parque.business.service.AttractionService;
import com.parque.business.service.GroupService;
import com.parque.business.service.TicketService;
import com.parque.business.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;

/**
 * Presentation layer: interprets text commands ({@code OPERATION;param;param...}) using String.split,
 * delegates to the {@link ParkController} and answers with a response whose first line starts with {@code OK}
 * or {@code ERROR:}. No exception escapes {@link #process(String)}. The same instance serves both interactive
 * and file input.
 */
public class CommandProcessor {
    /** Syntax of every available command, shown by HELP. Add new commands here. */
    public static final List<String> COMMAND_SYNTAX = List.of(
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
            "ATTRACTION CREATE;Identificador;Nombre;Aforo",
            "ATTRACTION ENTER;Identificador;Ticket",
            "ATTRACTION EXIT;Identificador;Ticket",
            "ATTRACTION STATUS;Identificador",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final ParkController controller;
    private boolean exitRequested;

    public CommandProcessor(ParkController controller) {
        this.controller = controller;
    }

    /** Convenience constructor: controller over the given services, starting at the OS local date. */
    public CommandProcessor(UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions) {
        this(new ParkController(users, groups, tickets, attractions));
    }

    /** Convenience constructor: controller over the given services, starting at {@code initialDate}. */
    public CommandProcessor(UserService users, GroupService groups, TicketService tickets,
            AttractionService attractions, LocalDate initialDate) {
        this(new ParkController(users, groups, tickets, attractions, initialDate));
    }

    public ParkController getController() {
        return controller;
    }

    /** The controller's current system date. */
    public LocalDate getCurrentDate() {
        return controller.getCurrentDate();
    }

    /** The controller's current individual ticket price. */
    public BigDecimal getTicketPrice() {
        return controller.getTicketPrice();
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
                controller.setSystemDate(parseDate(parts[1]));
                return ok("System date set to " + controller.getCurrentDate());
            }
            case "SYSTEM PRICE" -> {
                expectParameters(parts, 1, "SYSTEM PRICE;Precio");
                controller.setTicketPrice(parsePrice(parts[1]));
                return ok("Ticket price set to " + controller.getTicketPrice().toPlainString());
            }
            case "USER CREATE" -> {
                expectParameters(parts, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
                controller.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                return ok("User created");
            }
            case "USER DELETE" -> {
                expectParameters(parts, 2, "USER DELETE;Correo;Contraseña");
                controller.deleteUser(parts[1], parts[2]);
                return ok("User deleted");
            }
            case "USER UPDATE" -> {
                expectParameters(parts, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
                UserField field = UserField.fromName(parts[3]);
                if (field == null) {
                    throw new IllegalArgumentException("Field cannot be updated: " + parts[3]
                            + " (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                }
                controller.updateUser(parts[1], parts[2], field, parts[4]);
                return ok("User updated");
            }
            case "GROUP CREATE" -> {
                expectParameters(parts, 2, "GROUP CREATE;Identificador;Nombre");
                controller.createGroup(parts[1], parts[2]);
                return ok("Group created");
            }
            case "GROUP ADD" -> {
                expectParameters(parts, 2, "GROUP ADD;Identificador;Correo");
                controller.addGroupMember(parts[1], parts[2]);
                return ok("User added to group");
            }
            case "GROUP REMOVE" -> {
                expectParameters(parts, 2, "GROUP REMOVE;Identificador;Correo");
                controller.removeGroupMember(parts[1], parts[2]);
                return ok("User removed from group");
            }
            case "GROUP DELETE" -> {
                expectParameters(parts, 1, "GROUP DELETE;Identificador");
                controller.deleteGroup(parts[1]);
                return ok("Group deleted");
            }
            case "TICKET CREATE" -> {
                expectParameters(parts, 3, "TICKET CREATE;Identificador;Correo;yyyy-MM-dd");
                IndividualTicket ticket = controller.createTicket(parts[1], parts[2], parseDate(parts[3]));
                return "OK TICKET CREATED -> " + ticket.getPriceText();
            }
            case "TICKET DELETE" -> {
                expectParameters(parts, 1, "TICKET DELETE;Identificador");
                controller.deleteTicket(parts[1]);
                return ok("Ticket deleted");
            }
            case "GROUPTICKET CREATE" -> {
                expectParameters(parts, 3, "GROUPTICKET CREATE;Identificador;Grupo;yyyy-MM-dd");
                GroupTicket ticket = controller.createGroupTicket(parts[1], parts[2], parseDate(parts[3]));
                return "OK GROUP TICKET CREATED -> " + ticket.getPriceText();
            }
            case "GROUPTICKET DELETE" -> {
                expectParameters(parts, 1, "GROUPTICKET DELETE;Identificador");
                controller.deleteGroupTicket(parts[1]);
                return ok("Group ticket deleted");
            }
            case "ATTRACTION CREATE" -> {
                expectParameters(parts, 3, "ATTRACTION CREATE;Identificador;Nombre;Aforo");
                controller.createAttraction(parts[1], parts[2], parts[3]);
                return ok("Attraction created");
            }
            case "ATTRACTION ENTER" -> {
                expectParameters(parts, 2, "ATTRACTION ENTER;Identificador;Ticket");
                int entered = controller.enterAttraction(parts[1], parts[2]);
                return ok(entered + " user(s) entered attraction " + parts[1]);
            }
            case "ATTRACTION EXIT" -> {
                expectParameters(parts, 2, "ATTRACTION EXIT;Identificador;Ticket");
                int left = controller.exitAttraction(parts[1], parts[2]);
                return ok(left + " user(s) left attraction " + parts[1]);
            }
            case "ATTRACTION STATUS" -> {
                expectParameters(parts, 1, "ATTRACTION STATUS;Identificador");
                return ok(status(controller.attraction(parts[1])));
            }
            default -> throw new IllegalArgumentException("Unknown command: " + operation);
        }
    }

    private static String status(Attraction a) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder("Attraction " + a.getId() + " - " + a.getName());
        sb.append(nl).append("  Maximum capacity: ").append(a.getCapacity());
        sb.append(nl).append("  Users present: ").append(a.getPresent().size());
        sb.append(nl).append("  Available places: ").append(a.available());
        sb.append(nl).append("  Present:");
        if (a.getPresent().isEmpty()) {
            sb.append(" none");
        }
        for (User u : a.getPresent()) {
            sb.append(nl).append("    ").append(u.getName()).append(' ').append(u.getSurname())
                    .append(" <").append(u.getEmail()).append('>');
        }
        return sb.toString();
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
    public static BigDecimal parsePrice(String value) {
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
