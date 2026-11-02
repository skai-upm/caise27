package es.upm.etsisi.amusementpark.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.stream.Collectors;

import es.upm.etsisi.amusementpark.OperationException;
import es.upm.etsisi.amusementpark.attraction.Attraction;
import es.upm.etsisi.amusementpark.park.Park;
import es.upm.etsisi.amusementpark.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.user.User;
import es.upm.etsisi.amusementpark.user.UserField;

/**
 * Interprets and executes one command line. Elements are separated by ';' and
 * parsed with String.split; the same processor serves interactive and file input.
 * Parsing of values (dates, prices, capacities) happens here; business rules live in {@link Park}.
 */
public class CommandProcessor {
    static final String USER_CREATE = "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>";
    static final String USER_DELETE = "USER DELETE;<Email>;<Password>";
    static final String USER_UPDATE = "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>";
    static final String SYSTEM_DATE = "SYSTEM DATE;<yyyy-MM-dd>";
    static final String SYSTEM_PRICE = "SYSTEM PRICE;<Price>";
    static final String GROUP_CREATE = "GROUP CREATE;<Identifier>;<Name>";
    static final String GROUP_ADD = "GROUP ADD;<Identifier>;<Email>";
    static final String GROUP_REMOVE = "GROUP REMOVE;<Identifier>;<Email>";
    static final String GROUP_DELETE = "GROUP DELETE;<Identifier>";
    static final String TICKET_CREATE = "TICKET CREATE;<identifier>;<email>;<date>";
    static final String TICKET_DELETE = "TICKET DELETE;<identifier>";
    static final String GROUPTICKET_CREATE = "GROUPTICKET CREATE;<identifier>;<group>;<date>";
    static final String GROUPTICKET_DELETE = "GROUPTICKET DELETE;<identifier>";
    static final String ATTRACTION_CREATE = "ATTRACTION CREATE;<identifier>;<name>;<capacity>";
    static final String ATTRACTION_ENTER = "ATTRACTION ENTER;<attraction>;<ticket>";
    static final String ATTRACTION_EXIT = "ATTRACTION EXIT;<attraction>;<email>";
    static final String ATTRACTION_LIST = "ATTRACTION LIST;<attraction>";
    static final String ATTRACTION_STATUS = "ATTRACTION STATUS;<attractionIdentifier>";
    static final String HELP = "HELP";
    static final String EXIT = "EXIT";

    static final List<String> COMMANDS = List.of(USER_CREATE, USER_DELETE, USER_UPDATE, SYSTEM_DATE, SYSTEM_PRICE,
            GROUP_CREATE, GROUP_ADD, GROUP_REMOVE, GROUP_DELETE, TICKET_CREATE, TICKET_DELETE, GROUPTICKET_CREATE,
            GROUPTICKET_DELETE, ATTRACTION_CREATE, ATTRACTION_ENTER, ATTRACTION_EXIT, ATTRACTION_LIST,
            ATTRACTION_STATUS, HELP, EXIT);

    static final String HELP_TEXT = "Available commands:" + System.lineSeparator()
            + COMMANDS.stream().map(c -> "  " + c).collect(Collectors.joining(System.lineSeparator()))
            + System.lineSeparator() + "  (USER UPDATE fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int MAX_DECIMALS = 2;

    private final Park park;

    public CommandProcessor(Park park) {
        this.park = park;
    }

    public CommandResult process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String keyword = parts[0];
        String syntax = COMMANDS.stream().filter(c -> keyword(c).equals(keyword)).findFirst().orElse(null);
        if (syntax == null) {
            return CommandResult.error("unknown command '" + keyword + "'; type HELP for the list of commands");
        }
        if (parts.length != syntax.split(";", -1).length) {
            return CommandResult.error("wrong number of elements; usage: " + syntax);
        }
        try {
            return execute(syntax, parts);
        } catch (OperationException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    private static String keyword(String syntax) {
        int separator = syntax.indexOf(';');
        return separator < 0 ? syntax : syntax.substring(0, separator);
    }

    private CommandResult execute(String syntax, String[] p) throws OperationException {
        return switch (syntax) {
            case USER_CREATE -> {
                park.createUser(new User(p[1], p[2], p[3], p[4], p[5], p[6], p[7]));
                yield CommandResult.message("USER CREATED");
            }
            case USER_DELETE -> {
                park.deleteUser(p[1], p[2]);
                yield CommandResult.message("USER DELETED");
            }
            case USER_UPDATE -> updateUser(p);
            case SYSTEM_DATE -> {
                park.setCurrentDate(parseDate(p[1]));
                yield CommandResult.message("SYSTEM DATE SET: " + park.getCurrentDate());
            }
            case SYSTEM_PRICE -> {
                park.setBasePrice(parsePrice(p[1]));
                yield CommandResult.message("SYSTEM PRICE SET: " + euros(park.getBasePrice()));
            }
            case GROUP_CREATE, GROUP_ADD, GROUP_REMOVE, GROUP_DELETE -> group(syntax, p);
            case TICKET_CREATE, TICKET_DELETE, GROUPTICKET_CREATE, GROUPTICKET_DELETE -> ticket(syntax, p);
            case HELP -> CommandResult.message(HELP_TEXT);
            case EXIT -> new CommandResult("BYE", true);
            default -> attraction(syntax, p);
        };
    }

    private CommandResult updateUser(String[] p) throws OperationException {
        UserField field = UserField.fromName(p[3]);
        if (field == null) {
            throw new OperationException("field '" + p[3] + "' cannot be modified; allowed: NAME, SURNAME, EMAIL, "
                    + "PASSWORD, PHONE, CARD");
        }
        park.updateUser(p[1], p[2], field, p[4]);
        return CommandResult.message("USER UPDATED");
    }

    private CommandResult group(String syntax, String[] p) throws OperationException {
        return switch (syntax) {
            case GROUP_CREATE -> {
                park.createGroup(p[1], p[2]);
                yield CommandResult.message("GROUP CREATED");
            }
            case GROUP_ADD -> {
                park.addToGroup(p[1], p[2]);
                yield CommandResult.message("GROUP USER ADDED");
            }
            case GROUP_REMOVE -> {
                park.removeFromGroup(p[1], p[2]);
                yield CommandResult.message("GROUP USER REMOVED");
            }
            default -> {
                park.deleteGroup(p[1]);
                yield CommandResult.message("GROUP DELETED");
            }
        };
    }

    private CommandResult ticket(String syntax, String[] p) throws OperationException {
        return switch (syntax) {
            case TICKET_CREATE -> {
                IndividualTicket t = park.createTicket(p[1], p[2], parseDate(p[3]));
                yield CommandResult.message("TICKET CREATED. PRICE: " + euros(t.getPrice()));
            }
            case TICKET_DELETE -> {
                park.deleteTicket(p[1]);
                yield CommandResult.message("TICKET DELETED");
            }
            case GROUPTICKET_CREATE -> {
                GroupTicket t = park.createGroupTicket(p[1], p[2], parseDate(p[3]));
                yield CommandResult.message("GROUPTICKET CREATED. TOTAL PRICE: " + euros(t.getPrice()));
            }
            default -> {
                park.deleteGroupTicket(p[1]);
                yield CommandResult.message("GROUPTICKET DELETED");
            }
        };
    }

    private CommandResult attraction(String syntax, String[] p) throws OperationException {
        return switch (syntax) {
            case ATTRACTION_CREATE -> {
                park.createAttraction(p[1], p[2], parseCapacity(p[3]));
                yield CommandResult.message("ATTRACTION CREATED");
            }
            case ATTRACTION_ENTER -> {
                int entered = park.enter(p[1], p[2]);
                yield CommandResult.message("ATTRACTION ENTERED: " + entered + " user(s)");
            }
            case ATTRACTION_EXIT -> {
                park.exit(p[1], p[2]);
                yield CommandResult.message("ATTRACTION EXITED");
            }
            case ATTRACTION_LIST -> CommandResult.message(list(park.requireAttraction(p[1])));
            default -> CommandResult.message(status(park.requireAttraction(p[1])));
        };
    }

    private static String emails(Attraction a) {
        if (a.getOccupants().isEmpty()) {
            return "(none)";
        }
        return a.getOccupants().stream().map(User::getEmail).collect(Collectors.joining(", "));
    }

    private static String list(Attraction a) {
        return "USERS IN " + a.getIdentifier() + ": " + emails(a);
    }

    private static String status(Attraction a) {
        String nl = System.lineSeparator();
        return "ATTRACTION " + a.getIdentifier() + " - " + a.getName() + nl
                + "  Capacity: " + a.getCapacity() + nl
                + "  Inside: " + a.getOccupants().size() + nl
                + "  Available: " + a.getAvailablePlaces() + nl
                + "  Users: " + emails(a);
    }

    private static String euros(BigDecimal amount) {
        return amount.toPlainString() + " EUR";
    }

    static LocalDate parseDate(String text) throws OperationException {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new OperationException("invalid date '" + text + "'; expected yyyy-MM-dd");
        }
    }

    static BigDecimal parsePrice(String text) throws OperationException {
        int dot = text.indexOf('.');
        String whole = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        boolean valid = !whole.isEmpty() && isDigits(whole) && isDigits(decimals)
                && decimals.length() <= MAX_DECIMALS && (dot < 0 || !decimals.isEmpty());
        BigDecimal price = valid ? new BigDecimal(text) : BigDecimal.ZERO;
        if (price.signum() <= 0) {
            throw new OperationException("invalid price '" + text + "'; expected a positive amount such as 45.50");
        }
        return price;
    }

    static int parseCapacity(String text) throws OperationException {
        int capacity = isDigits(text) && !text.isEmpty() && text.length() < 10 ? Integer.parseInt(text) : 0;
        if (capacity <= 0) {
            throw new OperationException("invalid capacity '" + text + "'; expected a positive whole number");
        }
        return capacity;
    }

    private static boolean isDigits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
