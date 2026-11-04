package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Interprets text commands such as {@code USER CREATE;...}. Components are
 * separated with {@link String#split}. Every command yields exactly one
 * result line starting with {@code OK} or {@code ERROR}; this class never
 * throws for bad input.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final int CREATE_PARAMS = 7;
    private static final int DELETE_PARAMS = 2;
    private static final int UPDATE_PARAMS = 4;
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Syntax of every available command, shown by {@code HELP}. Extend when adding commands. */
    static final List<String> HELP_LINES = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor   (Campo: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private boolean exitRequested;
    /** The single simulated current date of this run; never persisted. */
    private LocalDate currentDate;
    /** Price of new individual tickets; starts at 40.00 on every run, never persisted. */
    private BigDecimal ticketPrice = DEFAULT_TICKET_PRICE;

    static final BigDecimal DEFAULT_TICKET_PRICE = new BigDecimal("40.00");
    private static final int PRICE_DECIMALS = 2;

    /** Creates the controller with the operating system's local date as current date. */
    public CommandProcessor(UserManager users, GroupManager groups, TicketManager tickets) {
        this(users, groups, tickets, LocalDate.now());
    }

    public CommandProcessor(UserManager users, GroupManager groups, TicketManager tickets,
                            LocalDate startupDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.currentDate = startupDate;
    }

    /** The price that the next individual ticket will be charged. */
    public BigDecimal getTicketPrice() {
        return ticketPrice;
    }

    /** The current system date that all time-dependent operations must use. */
    public LocalDate getCurrentDate() {
        return currentDate;
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
        currentDate = parseDate(p[0]);
        return ok("System date set to " + currentDate);
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
        users.create(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
        return ok("User " + p[3] + " created");
    }

    private String delete(String[] p) throws UserException {
        requireParams(p, DELETE_PARAMS, "USER DELETE;Correo;Contraseña");
        User user = users.authenticate(p[0], p[1]);
        if (tickets.hasTicketOnOrAfter(user, currentDate)) {
            throw new UserException("User " + p[0] + " has a ticket dated on or after " + currentDate
                    + " and cannot be deleted");
        }
        User deleted = users.delete(p[0], p[1]);
        tickets.detachUser(deleted);
        groups.removeUserEverywhere(deleted);
        return ok("User " + p[0] + " deleted");
    }

    private String ticketCreate(String[] p) throws UserException {
        requireParams(p, 3, "TICKET CREATE;Id;Correo;yyyy-MM-dd");
        Ticket ticket = tickets.create(p[0], p[1], parseDate(p[2]), currentDate, ticketPrice);
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String ticketDelete(String[] p) throws UserException {
        requireParams(p, 1, "TICKET DELETE;Id");
        tickets.delete(p[0], currentDate);
        return ok("Ticket " + p[0] + " deleted");
    }

    private String systemPrice(String[] p) throws UserException {
        requireParams(p, 1, "SYSTEM PRICE;Precio");
        ticketPrice = parsePrice(p[0]);
        return ok("Ticket price set to " + ticketPrice.toPlainString());
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
        groups.create(p[0], p[1]);
        return ok("Group " + p[0] + " created");
    }

    private String groupAdd(String[] p) throws UserException {
        requireParams(p, 2, "GROUP ADD;Id;Correo");
        groups.addMember(p[0], p[1]);
        return ok("User " + p[1] + " added to group " + p[0]);
    }

    private String groupRemove(String[] p) throws UserException {
        requireParams(p, 2, "GROUP REMOVE;Id;Correo");
        groups.removeMember(p[0], p[1]);
        return ok("User " + p[1] + " removed from group " + p[0]);
    }

    private String groupDelete(String[] p) throws UserException {
        requireParams(p, 1, "GROUP DELETE;Id");
        groups.delete(p[0]);
        return ok("Group " + p[0] + " deleted");
    }

    private String update(String[] p) throws UserException {
        requireParams(p, UPDATE_PARAMS, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = parseField(p[2]);
        users.update(p[0], p[1], field, p[3]);
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
