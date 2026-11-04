package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceLine;
import es.upm.etsisi.amusementpark.business.invoice.InvoiceSection;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.model.UserException;
import es.upm.etsisi.amusementpark.business.model.UserField;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Presentation layer: interprets text commands such as {@code USER CREATE;...}.
 * Components are separated with {@link String#split}, parameters are parsed and
 * the business operation is delegated to the {@link ParkController}. Every
 * command yields a result starting with {@code OK} or {@code ERROR}; this class
 * never throws for bad input.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final int CREATE_PARAMS = 7;
    private static final int DELETE_PARAMS = 2;
    private static final int UPDATE_PARAMS = 4;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Syntax of every available command, shown by {@code HELP}. Extend when adding commands. */
    public static final List<String> HELP_LINES = List.of(
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
            "ATTRACTION ENTER;IdAtraccion;IdTicket",
            "ATTRACTION EXIT;IdAtraccion;IdTicket",
            "ATTRACTION STATUS;IdAtraccion",
            "INVOICE CREATE;USER;Correo",
            "INVOICE CREATE;GROUP;IdGrupo",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private static final int PRICE_DECIMALS = 2;

    private final ParkController controller;
    private boolean exitRequested;

    public CommandProcessor(ParkController controller) {
        this.controller = controller;
    }

    /** The business controller that executes the commands. */
    public ParkController getController() {
        return controller;
    }

    /** True once an {@code EXIT} command has been processed. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Processes one command line.
     *
     * @return the result line, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(SEPARATOR, -1);
        String operation = normalizeOperation(parts[0]);
        String[] params = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            params[i - 1] = parts[i].trim();
        }
        try {
            return switch (operation) {
                case "USER CREATE" -> create(params);
                case "USER DELETE" -> delete(params);
                case "USER UPDATE" -> update(params);
                case "GROUP CREATE" -> groupCreate(params);
                case "GROUP ADD" -> groupAdd(params);
                case "GROUP REMOVE" -> groupRemove(params);
                case "GROUP DELETE" -> groupDelete(params);
                case "TICKET CREATE" -> ticketCreate(params);
                case "TICKET DELETE" -> ticketDelete(params);
                case "GROUPTICKET CREATE" -> groupTicketCreate(params);
                case "GROUPTICKET DELETE" -> groupTicketDelete(params);
                case "ATTRACTION CREATE" -> attractionCreate(params);
                case "ATTRACTION ENTER" -> attractionEnter(params);
                case "ATTRACTION EXIT" -> attractionExit(params);
                case "ATTRACTION STATUS" -> attractionStatus(params);
                case "INVOICE CREATE" -> invoiceCreate(params);
                case "SYSTEM DATE" -> systemDate(params);
                case "SYSTEM PRICE" -> systemPrice(params);
                case "HELP" -> help(params);
                case "EXIT" -> exit(params);
                default -> error("Unknown command: " + parts[0].trim());
            };
        } catch (UserException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected problem processing the command: " + e.getMessage());
        }
    }

    private String systemDate(String[] p) throws UserException {
        requireParams(p, 1, "SYSTEM DATE;yyyy-MM-dd");
        controller.setCurrentDate(parseDate(p[0]));
        return ok("System date set to " + controller.getCurrentDate());
    }

    private static String help(String[] p) throws UserException {
        requireParams(p, 0, "HELP");
        StringBuilder sb = new StringBuilder(ok("Available commands:"));
        for (String line : HELP_LINES) {
            sb.append(System.lineSeparator()).append("  ").append(line);
        }
        return sb.toString();
    }

    private String exit(String[] p) throws UserException {
        requireParams(p, 0, "EXIT");
        exitRequested = true;
        return ok("Bye");
    }

    private String create(String[] p) throws UserException {
        requireParams(p, CREATE_PARAMS, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        controller.createUser(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
        return ok("User " + p[3] + " created");
    }

    private String delete(String[] p) throws UserException {
        requireParams(p, DELETE_PARAMS, "USER DELETE;Correo;Contraseña");
        controller.deleteUser(p[0], p[1]);
        return ok("User " + p[0] + " deleted");
    }

    private String ticketCreate(String[] p) throws UserException {
        requireParams(p, 3, "TICKET CREATE;Id;Correo;yyyy-MM-dd");
        IndividualTicket ticket = controller.createTicket(p[0], p[1], parseDate(p[2]));
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String ticketDelete(String[] p) throws UserException {
        requireParams(p, 1, "TICKET DELETE;Id");
        controller.deleteTicket(p[0]);
        return ok("Ticket " + p[0] + " deleted");
    }

    private String systemPrice(String[] p) throws UserException {
        requireParams(p, 1, "SYSTEM PRICE;Precio");
        controller.setTicketPrice(parsePrice(p[0]));
        return ok("Ticket price set to " + controller.getTicketPrice().toPlainString());
    }

    /** Positive decimal with '.' as separator and at most two decimals (no regex). */
    private static BigDecimal parsePrice(String text) throws UserException {
        UserException invalid = new UserException("Invalid price '" + text
                + "', expected a positive amount such as 60.00");
        int dot = text.indexOf('.');
        String integerPart = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (integerPart.isEmpty() || !isDigits(integerPart) || (dot >= 0 && (decimals.isEmpty()
                || decimals.length() > PRICE_DECIMALS || !isDigits(decimals)))) {
            throw invalid;
        }
        BigDecimal price = new BigDecimal(text).setScale(PRICE_DECIMALS);
        if (price.signum() <= 0) {
            throw invalid;
        }
        return price;
    }

    private static boolean isDigits(String value) {
        for (char c : value.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private static LocalDate parseDate(String text) throws UserException {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new UserException("Invalid date '" + text + "', expected yyyy-MM-dd");
        }
    }

    private String groupCreate(String[] p) throws UserException {
        requireParams(p, 2, "GROUP CREATE;Id;Nombre");
        controller.createGroup(p[0], p[1]);
        return ok("Group " + p[0] + " created");
    }

    private String groupAdd(String[] p) throws UserException {
        requireParams(p, 2, "GROUP ADD;Id;Correo");
        controller.addGroupMember(p[0], p[1]);
        return ok("User " + p[1] + " added to group " + p[0]);
    }

    private String groupRemove(String[] p) throws UserException {
        requireParams(p, 2, "GROUP REMOVE;Id;Correo");
        controller.removeGroupMember(p[0], p[1]);
        return ok("User " + p[1] + " removed from group " + p[0]);
    }

    private String groupDelete(String[] p) throws UserException {
        requireParams(p, 1, "GROUP DELETE;Id");
        controller.deleteGroup(p[0]);
        return ok("Group " + p[0] + " deleted");
    }

    private String invoiceCreate(String[] p) throws UserException {
        requireParams(p, 2, "INVOICE CREATE;USER;Correo | INVOICE CREATE;GROUP;IdGrupo");
        Invoice invoice = switch (p[0].toUpperCase(Locale.ROOT)) {
            case "USER" -> controller.createUserInvoice(p[1]);
            case "GROUP" -> controller.createGroupInvoice(p[1]);
            default -> throw new UserException("Unknown invoice recipient type: " + p[0] + " (use USER or GROUP)");
        };
        StringBuilder sb = new StringBuilder(ok(invoice.getTitle()));
        for (InvoiceSection section : invoice.getSections()) {
            sb.append(System.lineSeparator()).append("  ").append(section.getTitle()).append(':');
            for (InvoiceLine line : section.getLines()) {
                sb.append(System.lineSeparator()).append("    ").append(line.getDescription()).append(": ")
                        .append(line.getAmount().toPlainString());
            }
        }
        sb.append(System.lineSeparator()).append("  TOTAL: ").append(invoice.getTotal().toPlainString());
        return sb.toString();
    }

    private String attractionCreate(String[] p) throws UserException {
        requireParams(p, 3, "ATTRACTION CREATE;Id;Nombre;Capacidad");
        controller.createAttraction(p[0], p[1], p[2]);
        return ok("Attraction " + p[0] + " created");
    }

    private String attractionEnter(String[] p) throws UserException {
        requireParams(p, 2, "ATTRACTION ENTER;IdAtraccion;IdTicket");
        Ticket ticket = controller.enterAttraction(p[0], p[1]);
        return ok(ticket.getVisitors().size() + " visitor(s) entered " + p[0] + " with ticket " + p[1]);
    }

    private String attractionExit(String[] p) throws UserException {
        requireParams(p, 2, "ATTRACTION EXIT;IdAtraccion;IdTicket");
        controller.exitAttraction(p[0], p[1]);
        return ok("Visitors of ticket " + p[1] + " left " + p[0]);
    }

    private String attractionStatus(String[] p) throws UserException {
        requireParams(p, 1, "ATTRACTION STATUS;IdAtraccion");
        Attraction a = controller.getAttraction(p[0]);
        StringBuilder sb = new StringBuilder(ok("Attraction " + a.getId() + " (" + a.getName() + ") capacity="
                + a.getCapacity() + " present=" + a.getPresent().size() + " available=" + a.getAvailable()));
        for (User u : a.getPresent()) {
            sb.append(System.lineSeparator()).append("  - ").append(u.getName()).append(' ')
                    .append(u.getSurname()).append(" <").append(u.getEmail()).append('>');
        }
        return sb.toString();
    }

    private String groupTicketCreate(String[] p) throws UserException {
        requireParams(p, 3, "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
        Group group = controller.requireGroup(p[1]);
        GroupTicket ticket = controller.createGroupTicket(p[0], group, parseDate(p[2]));
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String groupTicketDelete(String[] p) throws UserException {
        requireParams(p, 1, "GROUPTICKET DELETE;Id");
        controller.deleteGroupTicket(p[0]);
        return ok("Group ticket " + p[0] + " deleted");
    }

    private String update(String[] p) throws UserException {
        requireParams(p, UPDATE_PARAMS, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = parseField(p[2]);
        controller.updateUser(p[0], p[1], field, p[3]);
        return ok("User " + p[0] + " updated: " + field);
    }

    private static UserField parseField(String text) throws UserException {
        String name = text.toUpperCase(Locale.ROOT);
        if ("DNI".equals(name)) {
            throw new UserException("The DNI cannot be modified");
        }
        for (UserField f : UserField.values()) {
            if (f.name().equals(name)) {
                return f;
            }
        }
        throw new UserException("Unknown field: " + text);
    }

    private static void requireParams(String[] params, int expected, String format) throws UserException {
        if (params.length != expected) {
            throw new UserException("Expected " + expected + " parameters, got " + params.length
                    + ". Format: " + format);
        }
    }

    /** Joins the words of the operation with single spaces and upper-cases them. */
    private static String normalizeOperation(String raw) {
        List<String> words = new ArrayList<>();
        for (String w : raw.trim().split(" ")) {
            if (!w.isEmpty()) {
                words.add(w.toUpperCase(Locale.ROOT));
            }
        }
        return String.join(" ", words);
    }

    private static String ok(String message) {
        return "OK: " + message;
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
