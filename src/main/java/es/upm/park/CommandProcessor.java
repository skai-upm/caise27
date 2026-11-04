package es.upm.park;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main controller of the application: interprets API-like text commands and owns the
 * execution-wide settings (current system date and ticket price) used by every operation.
 * <p>
 * Interprets API-like text commands. A command is split on {@code ;} into the operation and
 * its parameters; the operation is split on spaces into its words. Invalid input always
 * produces an error result and never throws.
 */
public class CommandProcessor {

    private static final String PARAM_SEPARATOR = ";";
    private static final String WORD_SEPARATOR = " ";

    /** Syntax of every available command, shown by {@code HELP}. Add new commands here. */
    private static final List<String> COMMAND_SYNTAX = List.of(
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
            "SYSTEM DATE;yyyy-MM-dd",
            "SYSTEM PRICE;Precio",
            "HELP",
            "EXIT");

    private static final int DATE_LENGTH = "yyyy-MM-dd".length();

    /** Ticket price at the start of every execution. */
    static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");
    private static final int PRICE_DECIMALS = 2;

    private final UserManager users;
    private final GroupManager groups;
    private final TicketManager tickets;
    private LocalDate currentDate;
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

    /** The price that the next ticket will capture. */
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /** The current system date; time-dependent operations must use this, never the real clock. */
    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public Result process(String line) {
        if (line == null || line.isBlank()) {
            return Result.error("Empty command.");
        }
        String[] parts = line.split(PARAM_SEPARATOR, -1);
        String[] params = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            params[i - 1] = parts[i].trim();
        }
        List<String> words = new ArrayList<>();
        for (String w : parts[0].trim().split(WORD_SEPARATOR)) {
            if (!w.isEmpty()) {
                words.add(w);
            }
        }
        if (words.size() == 1 && words.get(0).equals("HELP")) {
            return params.length == 0 ? help() : Result.error("Usage: HELP");
        }
        if (words.size() == 1 && words.get(0).equals("EXIT")) {
            return params.length == 0 ? Result.exit("Bye.") : Result.error("Usage: EXIT");
        }
        if (words.size() == 2 && words.get(0).equals("SYSTEM") && words.get(1).equals("DATE")) {
            return systemDate(params);
        }
        if (words.size() == 2 && words.get(0).equals("SYSTEM") && words.get(1).equals("PRICE")) {
            return systemPrice(params);
        }
        if (words.size() == 2 && words.get(0).equals("TICKET")) {
            return ticket(words.get(1), params, parts[0].trim());
        }
        if (words.size() == 2 && words.get(0).equals("GROUPTICKET")) {
            return groupTicket(words.get(1), params, parts[0].trim());
        }
        if (words.size() == 2 && words.get(0).equals("GROUP")) {
            return group(words.get(1), params, parts[0].trim());
        }
        if (words.size() != 2 || !words.get(0).equals("USER")) {
            return Result.error("Unknown command: " + parts[0].trim());
        }
        return switch (words.get(1)) {
            case "CREATE" -> userCreate(params);
            case "DELETE" -> userDelete(params);
            case "UPDATE" -> userUpdate(params);
            default -> Result.error("Unknown command: " + parts[0].trim());
        };
    }

    private Result help() {
        return Result.ok("Available commands:" + System.lineSeparator()
                + String.join(System.lineSeparator(), COMMAND_SYNTAX));
    }

    private Result systemDate(String[] p) {
        if (!hasParams(p, 1)) {
            return Result.error("Usage: SYSTEM DATE;yyyy-MM-dd");
        }
        Optional<LocalDate> date = parseDate(p[0]);
        if (date.isEmpty()) {
            return Result.error("Invalid date '" + p[0] + "': expected format yyyy-MM-dd.");
        }
        currentDate = date.get();
        return Result.ok("System date set to " + currentDate + ".");
    }

    private Result systemPrice(String[] p) {
        if (!hasParams(p, 1)) {
            return Result.error("Usage: SYSTEM PRICE;Precio");
        }
        Optional<BigDecimal> price = parsePrice(p[0]);
        if (price.isEmpty()) {
            return Result.error("Invalid price '" + p[0] + "': expected a non-negative amount such as 60.00.");
        }
        currentPrice = price.get();
        return Result.ok("Ticket price set to " + currentPrice.toPlainString() + ".");
    }

    private Result ticket(String operation, String[] p, String command) {
        switch (operation) {
            case "CREATE" -> {
                if (!hasParams(p, 3)) {
                    return Result.error("Usage: TICKET CREATE;Id;Correo;yyyy-MM-dd");
                }
                Optional<LocalDate> date = parseDate(p[2]);
                if (date.isEmpty()) {
                    return Result.error("Invalid date '" + p[2] + "': expected format yyyy-MM-dd.");
                }
                return tickets.create(p[0], p[1], date.get(), currentDate, currentPrice);
            }
            case "DELETE" -> {
                return hasParams(p, 1) ? tickets.delete(p[0], currentDate) : Result.error("Usage: TICKET DELETE;Id");
            }
            default -> {
                return Result.error("Unknown command: " + command);
            }
        }
    }

    private Result groupTicket(String operation, String[] p, String command) {
        switch (operation) {
            case "CREATE" -> {
                if (!hasParams(p, 3)) {
                    return Result.error("Usage: GROUPTICKET CREATE;Id;IdGrupo;yyyy-MM-dd");
                }
                Optional<Group> group = groups.find(p[1]);
                if (group.isEmpty()) {
                    return Result.error("Group " + p[1] + " does not exist.");
                }
                Optional<LocalDate> date = parseDate(p[2]);
                if (date.isEmpty()) {
                    return Result.error("Invalid date '" + p[2] + "': expected format yyyy-MM-dd.");
                }
                return tickets.createGroupTicket(p[0], group.get(), date.get(), currentDate, currentPrice);
            }
            case "DELETE" -> {
                return hasParams(p, 1) ? tickets.deleteGroupTicket(p[0], currentDate)
                        : Result.error("Usage: GROUPTICKET DELETE;Id");
            }
            default -> {
                return Result.error("Unknown command: " + command);
            }
        }
    }

    /** Strict {@code yyyy-MM-dd} calendar date. */
    private static Optional<LocalDate> parseDate(String text) {
        if (text.length() != DATE_LENGTH) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    /** Non-negative amount: digits, optionally a point and at most two decimals. */
    private static Optional<BigDecimal> parsePrice(String text) {
        int point = text.indexOf('.');
        String whole = point < 0 ? text : text.substring(0, point);
        String decimals = point < 0 ? "" : text.substring(point + 1);
        if (whole.isEmpty() || !isAsciiDigits(whole) || !isAsciiDigits(decimals)
                || decimals.length() > PRICE_DECIMALS || (point >= 0 && decimals.isEmpty())) {
            return Optional.empty();
        }
        return Optional.of(new BigDecimal(text).setScale(PRICE_DECIMALS, RoundingMode.UNNECESSARY));
    }

    private static boolean isAsciiDigits(String s) {
        for (char c : s.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private Result userCreate(String[] p) {
        if (!hasParams(p, 7)) {
            return Result.error("Usage: USER CREATE;Nombre;Apellidos;DNI;Correo;Contraseña;Telefono;Tarjeta");
        }
        return users.create(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
    }

    private Result userDelete(String[] p) {
        if (!hasParams(p, 2)) {
            return Result.error("Usage: USER DELETE;Correo;Contraseña");
        }
        Optional<User> target = users.authenticate(p[0], p[1]);
        if (target.isPresent() && tickets.hasTicketOnOrAfter(target.get(), currentDate)) {
            return Result.error("User " + p[0] + " has tickets dated on or after " + currentDate + " and cannot be deleted.");
        }
        Result result = users.delete(p[0], p[1]);
        if (result.success() && target.isPresent()) {
            Result cleanup = groups.removeUserEverywhere(target.get());
            if (!cleanup.success()) {
                return Result.error(result.message() + " " + cleanup.message());
            }
        }
        return result;
    }

    private Result group(String operation, String[] p, String command) {
        if (!operation.equals("CREATE") && p.length > 0) {
            Optional<Group> target = groups.find(p[0]);
            if (target.isPresent() && tickets.isGroupLocked(target.get(), currentDate)) {
                return Result.error("Group " + p[0] + " has group tickets dated on or after " + currentDate
                        + " and cannot be modified or deleted.");
            }
        }
        return switch (operation) {
            case "CREATE" -> hasParams(p, 2) ? groups.create(p[0], p[1]) : Result.error("Usage: GROUP CREATE;Id;Nombre");
            case "ADD" -> hasParams(p, 2) ? groups.addMember(p[0], p[1]) : Result.error("Usage: GROUP ADD;Id;Correo");
            case "REMOVE" -> hasParams(p, 2) ? groups.removeMember(p[0], p[1]) : Result.error("Usage: GROUP REMOVE;Id;Correo");
            case "DELETE" -> hasParams(p, 1) ? groups.delete(p[0]) : Result.error("Usage: GROUP DELETE;Id");
            default -> Result.error("Unknown command: " + command);
        };
    }

    private Result userUpdate(String[] p) {
        if (!hasParams(p, 4)) {
            return Result.error("Usage: USER UPDATE;Correo;Contraseña;Campo;NuevoValor");
        }
        UserField field;
        try {
            field = UserField.valueOf(p[2]);
        } catch (IllegalArgumentException e) {
            return Result.error("Field " + p[2] + " cannot be updated. Editable fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD.");
        }
        return users.update(p[0], p[1], field, p[3]);
    }

    /** Exactly {@code count} parameters, none of them empty. */
    private static boolean hasParams(String[] p, int count) {
        if (p.length != count) {
            return false;
        }
        for (String s : p) {
            if (s.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
