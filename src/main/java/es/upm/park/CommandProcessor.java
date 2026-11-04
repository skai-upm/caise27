package es.upm.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Interprets text commands of the form {@code OPERATION;param1;param2;...}
 * using {@link String#split(String, int)} and returns one result message.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    /** Syntax of every available command, shown by HELP. Extend when adding commands. */
    static final List<String> USAGE = List.of(
            "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta",
            "USER DELETE;Correo;Contraseña",
            "USER UPDATE;Correo;Contraseña;Campo;NuevoValor",
            "GROUP CREATE;Id;Nombre",
            "GROUP ADD;Id;Correo",
            "GROUP REMOVE;Id;Correo",
            "GROUP DELETE;Id",
            "TICKET CREATE;Id;Correo;yyyy-MM-dd",
            "TICKET DELETE;Id",
            "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd",
            "GROUPTICKET DELETE;IdTicket",
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");
    private static final int PRICE_DECIMALS = 2;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private boolean exitRequested;
    /** The single current date of the application; time-dependent rules must use it. */
    private LocalDate currentDate;
    /** Current individual-ticket price; starts at 40.00 on every execution, never persisted. */
    private BigDecimal currentPrice = DEFAULT_PRICE;

    /** Creates the controller with the operating system's local date as current date. */
    public CommandProcessor(UserManager users, GroupManager groups, TicketManager tickets) {
        this(users, groups, tickets, LocalDate.now());
    }

    public CommandProcessor(UserManager users, GroupManager groups, TicketManager tickets, LocalDate initialDate) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.currentDate = initialDate;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public String process(String line) {
        if (line == null || line.isBlank()) {
            return "ERROR: empty command";
        }
        String[] parts = line.split(SEPARATOR, -1);
        String operation = parts[0].trim();
        try {
            return switch (operation) {
                case "USER CREATE" -> create(parts);
                case "USER DELETE" -> delete(parts);
                case "USER UPDATE" -> update(parts);
                case "GROUP CREATE" -> groupCreate(parts);
                case "GROUP ADD" -> groupAdd(parts);
                case "GROUP REMOVE" -> groupRemove(parts);
                case "GROUP DELETE" -> groupDelete(parts);
                case "TICKET CREATE" -> ticketCreate(parts);
                case "TICKET DELETE" -> ticketDelete(parts);
                case "GROUPTICKET CREATE" -> groupTicketCreate(parts);
                case "GROUPTICKET DELETE" -> groupTicketDelete(parts);
                case "SYSTEM DATE" -> systemDate(parts);
                case "SYSTEM PRICE" -> systemPrice(parts);
                case "HELP" -> help(parts);
                case "EXIT" -> exit(parts);
                default -> "ERROR: unknown command '" + operation + "'";
            };
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: command could not be completed (" + e.getMessage() + ")";
        }
    }

    /** True once EXIT has been processed; input loops must stop reading. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String help(String[] p) {
        requireParams(p, 0, "HELP");
        StringBuilder sb = new StringBuilder("OK: available commands:");
        for (String usage : USAGE) {
            sb.append(System.lineSeparator()).append("  ").append(usage);
        }
        sb.append(System.lineSeparator()).append("  Campo (USER UPDATE): ")
                .append(String.join(", ", Arrays.stream(UserField.values()).map(Enum::name).toList()));
        return sb.toString();
    }

    private String groupCreate(String[] p) {
        requireParams(p, 2, "GROUP CREATE;Id;Nombre");
        groups.create(p[1], p[2]);
        return "OK: group " + p[1] + " created";
    }

    private String groupAdd(String[] p) {
        requireParams(p, 2, "GROUP ADD;Id;Correo");
        requireUnlocked(p[1]);
        groups.addMember(p[1], p[2]);
        return "OK: user " + p[2] + " added to group " + p[1];
    }

    private String groupRemove(String[] p) {
        requireParams(p, 2, "GROUP REMOVE;Id;Correo");
        requireUnlocked(p[1]);
        groups.removeMember(p[1], p[2]);
        return "OK: user " + p[2] + " removed from group " + p[1];
    }

    private String groupDelete(String[] p) {
        requireParams(p, 1, "GROUP DELETE;Id");
        requireUnlocked(p[1]);
        groups.delete(p[1]);
        return "OK: group " + p[1] + " deleted";
    }

    /** A group with a current or future group ticket cannot be modified or deleted. */
    private void requireUnlocked(String groupId) {
        Group group = groups.find(groupId);
        if (group != null && tickets.hasGroupTicketOnOrAfter(group, currentDate)) {
            throw new IllegalArgumentException("group " + groupId
                    + " is locked by a group ticket dated on or after " + currentDate);
        }
    }

    private String groupTicketCreate(String[] p) {
        requireParams(p, 3, "GROUPTICKET CREATE;IdTicket;IdGrupo;yyyy-MM-dd");
        Group group = groups.find(p[2]);
        if (group == null) {
            throw new IllegalArgumentException("no group with identifier " + p[2]);
        }
        GroupTicket ticket = tickets.createGroupTicket(p[1], group, parseDate(p[3]), currentPrice, currentDate);
        return "OK GROUP TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String groupTicketDelete(String[] p) {
        requireParams(p, 1, "GROUPTICKET DELETE;IdTicket");
        tickets.deleteGroupTicket(p[1], currentDate);
        return "OK: group ticket " + p[1] + " deleted";
    }

    private String ticketCreate(String[] p) {
        requireParams(p, 3, "TICKET CREATE;Id;Correo;yyyy-MM-dd");
        User owner = users.findByEmail(p[2]);
        if (owner == null) {
            throw new IllegalArgumentException("no registered user with email " + p[2]);
        }
        IndividualTicket ticket = tickets.create(p[1], owner, parseDate(p[3]), currentPrice, currentDate);
        return "OK TICKET CREATED -> " + ticket.getPrice().toPlainString();
    }

    private String ticketDelete(String[] p) {
        requireParams(p, 1, "TICKET DELETE;Id");
        tickets.delete(p[1], currentDate);
        return "OK: ticket " + p[1] + " deleted";
    }

    private String systemDate(String[] p) {
        requireParams(p, 1, "SYSTEM DATE;yyyy-MM-dd");
        currentDate = parseDate(p[1]);
        return "OK: system date set to " + currentDate;
    }

    private String systemPrice(String[] p) {
        requireParams(p, 1, "SYSTEM PRICE;Precio");
        currentPrice = parsePrice(p[1]);
        return "OK: ticket price set to " + currentPrice.toPlainString();
    }

    private static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("invalid date '" + value + "', expected format yyyy-MM-dd");
        }
    }

    /** Positive decimal: digits, optionally '.' and one or two decimals (no regex). */
    static BigDecimal parsePrice(String value) {
        String error = "invalid price '" + value + "', expected a positive amount such as 60.00";
        int dot = value.indexOf('.');
        String integer = dot < 0 ? value : value.substring(0, dot);
        String decimals = dot < 0 ? "" : value.substring(dot + 1);
        boolean wellFormed = !integer.isEmpty() && isDigits(integer) && isDigits(decimals)
                && (dot < 0 || (!decimals.isEmpty() && decimals.length() <= PRICE_DECIMALS));
        if (!wellFormed) {
            throw new IllegalArgumentException(error);
        }
        BigDecimal price = new BigDecimal(value).setScale(PRICE_DECIMALS, RoundingMode.UNNECESSARY);
        if (price.signum() <= 0) {
            throw new IllegalArgumentException(error);
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

    private String exit(String[] p) {
        requireParams(p, 0, "EXIT");
        exitRequested = true;
        return "OK: application terminated";
    }

    private String create(String[] p) {
        requireParams(p, 7, "USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        return "OK: user " + p[4] + " created";
    }

    private String delete(String[] p) {
        requireParams(p, 2, "USER DELETE;Correo;Contraseña");
        User user = users.authenticate(p[1], p[2]);
        if (tickets.hasTicketOnOrAfter(user, currentDate)) {
            throw new IllegalArgumentException("user " + p[1] + " has tickets dated on or after " + currentDate);
        }
        User removed = users.delete(p[1], p[2]);
        groups.removeUserFromAllGroups(removed);
        return "OK: user " + p[1] + " deleted";
    }

    private String update(String[] p) {
        requireParams(p, 4, "USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        UserField field = parseField(p[3]);
        users.update(p[1], p[2], field, p[4]);
        return "OK: user " + p[1] + " updated (" + field + ")";
    }

    private static UserField parseField(String name) {
        try {
            return UserField.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unsupported field '" + name + "'");
        }
    }

    private static void requireParams(String[] parts, int expected, String usage) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("expected " + expected + " parameters, usage: " + usage);
        }
    }
}
