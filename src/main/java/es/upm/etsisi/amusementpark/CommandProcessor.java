package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;

/**
 * Interprets API-like text commands, split with {@link String#split}, and returns a one-line response
 * starting with {@code OK:} or {@code ERROR:}. Never throws for invalid input.
 * It is the application's main controller and holds the single current system date and the current
 * ticket price of the execution (neither is persisted).
 *
 * <pre>
 * USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta
 * USER DELETE;Correo;Contraseña
 * USER UPDATE;Correo;Contraseña;Campo;NuevoValor
 * GROUP CREATE;Id;Nombre
 * GROUP ADD;Id;Correo
 * GROUP REMOVE;Id;Correo
 * GROUP DELETE;Id
 * TICKET CREATE;Id;Correo;yyyy-MM-dd
 * TICKET DELETE;Id
 * GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd
 * GROUPTICKET DELETE;Id
 * SYSTEM DATE;yyyy-MM-dd
 * SYSTEM PRICE;Precio
 * HELP
 * EXIT
 * </pre>
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";
    private static final int CREATE_TOKENS = 8;
    private static final int DELETE_TOKENS = 3;
    private static final int UPDATE_TOKENS = 5;
    private static final int DATE_TOKENS = 2;
    private static final int GROUP_CREATE_TOKENS = 3;
    private static final int GROUP_MEMBER_TOKENS = 3;
    private static final int GROUP_DELETE_TOKENS = 2;
    private static final int TICKET_CREATE_TOKENS = 4;
    private static final int TICKET_DELETE_TOKENS = 2;
    private static final int PRICE_TOKENS = 2;
    private static final int PRICE_DECIMALS = 2;
    /** Ticket price at the start of every execution. */
    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Syntax of every available command, shown by HELP. Extend it when new commands are added. */
    static final List<String> COMMAND_SYNTAX = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;Id",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private final UserService users;
    private final GroupService groups;
    private final TicketService tickets;
    private boolean exitRequested;
    private LocalDate currentDate;
    private BigDecimal currentPrice = DEFAULT_PRICE;

    /** Creates the controller with the operating system's local date as current date. */
    public CommandProcessor(UserService users, GroupService groups, TicketService tickets) {
        this(users, groups, tickets, LocalDate.now());
    }

    public CommandProcessor(UserService users, GroupService groups, TicketService tickets, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.currentDate = initialDate;
    }

    /** The current ticket price, applied to tickets created from now on. */
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /** The single current date of the execution; every time-dependent rule must use it. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    /** True once EXIT has been processed: no further command must be executed. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    public String process(String line) {
        if (line == null || line.isBlank()) {
            return error("Empty command");
        }
        String[] tokens = line.split(PARAM_SEPARATOR, -1);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }
        String operation = tokens[0].toUpperCase(Locale.ROOT);
        try {
            return switch (operation) {
                case "USER CREATE" -> create(tokens);
                case "USER DELETE" -> delete(tokens);
                case "USER UPDATE" -> update(tokens);
                case "GROUP CREATE" -> groupCreate(tokens);
                case "GROUP ADD" -> groupAdd(tokens);
                case "GROUP REMOVE" -> groupRemove(tokens);
                case "GROUP DELETE" -> groupDelete(tokens);
                case "TICKET CREATE" -> ticketCreate(tokens);
                case "TICKET DELETE" -> ticketDelete(tokens);
                case "GROUPTICKET CREATE" -> groupTicketCreate(tokens);
                case "GROUPTICKET DELETE" -> groupTicketDelete(tokens);
                case "SYSTEM DATE" -> systemDate(tokens);
                case "SYSTEM PRICE" -> systemPrice(tokens);
                case "HELP" -> tokens.length == 1 ? help() : usage("HELP");
                case "EXIT" -> tokens.length == 1 ? exit() : usage("EXIT");
                default -> error("Unknown command: " + tokens[0]);
            };
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Operation failed: " + e.getMessage());
        }
    }

    private String create(String[] t) {
        if (t.length != CREATE_TOKENS) {
            return usage("USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        User u = users.create(t[1], t[2], t[3], t[4], t[5], t[6], t[7]);
        return "OK: User " + u.getEmail() + " created";
    }

    private String delete(String[] t) {
        if (t.length != DELETE_TOKENS) {
            return usage("USER DELETE;Correo;Contraseña");
        }
        User user = users.authenticate(t[1], t[2]);
        if (tickets.hasActiveTickets(user, currentDate)) {
            return error("User " + t[1] + " cannot be deleted: it has tickets dated on or after " + currentDate);
        }
        User deleted = users.delete(t[1], t[2]);
        groups.removeUserFromAll(deleted);
        return "OK: User " + t[1] + " deleted";
    }

    private String update(String[] t) {
        if (t.length != UPDATE_TOKENS) {
            return usage("USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field = UserField.parse(t[3]);
        if (field == null) {
            return error("Field " + t[3] + " cannot be updated (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
        }
        users.update(t[1], t[2], field, t[4]);
        return "OK: User " + t[1] + " updated (" + field + ")";
    }

    private String groupCreate(String[] t) {
        if (t.length != GROUP_CREATE_TOKENS) {
            return usage("GROUP CREATE;Id;Nombre");
        }
        groups.create(t[1], t[2]);
        return "OK: Group " + t[1] + " created";
    }

    private String groupAdd(String[] t) {
        if (t.length != GROUP_MEMBER_TOKENS) {
            return usage("GROUP ADD;Id;Correo");
        }
        requireUnlocked(t[1]);
        groups.addMember(t[1], t[2]);
        return "OK: User " + t[2] + " added to group " + t[1];
    }

    private String groupRemove(String[] t) {
        if (t.length != GROUP_MEMBER_TOKENS) {
            return usage("GROUP REMOVE;Id;Correo");
        }
        requireUnlocked(t[1]);
        groups.removeMember(t[1], t[2]);
        return "OK: User " + t[2] + " removed from group " + t[1];
    }

    private String groupDelete(String[] t) {
        if (t.length != GROUP_DELETE_TOKENS) {
            return usage("GROUP DELETE;Id");
        }
        requireUnlocked(t[1]);
        groups.delete(t[1]);
        return "OK: Group " + t[1] + " deleted";
    }

    private String ticketCreate(String[] t) {
        if (t.length != TICKET_CREATE_TOKENS) {
            return usage("TICKET CREATE;Id;Correo;yyyy-MM-dd");
        }
        Ticket ticket = tickets.create(t[1], t[2], parseDate(t[3]), currentDate, currentPrice);
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String ticketDelete(String[] t) {
        if (t.length != TICKET_DELETE_TOKENS) {
            return usage("TICKET DELETE;Id");
        }
        tickets.delete(t[1], currentDate);
        return "OK: Ticket " + t[1] + " deleted";
    }

    /** Rejects changes to a group that has a group ticket dated on or after the system date. */
    private void requireUnlocked(String groupId) {
        groups.find(groupId).filter(g -> tickets.isGroupLocked(g, currentDate)).ifPresent(g -> {
            throw new IllegalArgumentException("Group " + groupId + " is locked by a group ticket dated on or after "
                    + currentDate);
        });
    }

    private String groupTicketCreate(String[] t) {
        if (t.length != TICKET_CREATE_TOKENS) {
            return usage("GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
        }
        Group group = groups.find(t[2])
                .orElseThrow(() -> new IllegalArgumentException("No group with identifier " + t[2]));
        GroupTicket ticket = tickets.createGroupTicket(t[1], group, parseDate(t[3]), currentDate, currentPrice);
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String groupTicketDelete(String[] t) {
        if (t.length != TICKET_DELETE_TOKENS) {
            return usage("GROUPTICKET DELETE;Id");
        }
        tickets.deleteGroupTicket(t[1], currentDate);
        return "OK: Group ticket " + t[1] + " deleted";
    }

    private String systemDate(String[] t) {
        if (t.length != DATE_TOKENS) {
            return usage("SYSTEM DATE;yyyy-MM-dd");
        }
        currentDate = parseDate(t[1]);
        return "OK: System date set to " + currentDate;
    }

    private String systemPrice(String[] t) {
        if (t.length != PRICE_TOKENS) {
            return usage("SYSTEM PRICE;Precio");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(t[1]);
        } catch (NumberFormatException e) {
            return error("Invalid price " + t[1]);
        }
        if (price.signum() < 0 || price.scale() > PRICE_DECIMALS) {
            return error("Invalid price " + t[1] + ": it must be a non-negative amount with at most two decimals");
        }
        currentPrice = price.setScale(PRICE_DECIMALS);
        return "OK: Ticket price set to " + currentPrice.toPlainString();
    }

    /** Parses a strict {@code yyyy-MM-dd} date. */
    private static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date " + text + ". Expected format yyyy-MM-dd", e);
        }
    }

    private static String help() {
        return "Available commands:" + System.lineSeparator() + "  "
                + String.join(System.lineSeparator() + "  ", COMMAND_SYNTAX);
    }

    private String exit() {
        exitRequested = true;
        return "OK: Bye";
    }

    private static String usage(String format) {
        return error("Wrong number of parameters. Usage: " + format);
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
