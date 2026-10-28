package es.upm.amusementpark.cli;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.service.AttractionStatus;
import es.upm.amusementpark.service.OperationException;
import es.upm.amusementpark.service.UserService;

/**
 * Interprets text commands whose elements are separated by {@code ;} and returns the response to
 * print.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);
    private static final int PRICE_DECIMALS = 2;
    private static final Set<String> STATE_CHANGING = Set.of("USER CREATE", "USER DELETE", "USER UPDATE",
            "GROUP CREATE", "GROUP ADD", "GROUP REMOVE", "GROUP DELETE", "TICKET CREATE", "TICKET DELETE",
            "GROUPTICKET CREATE", "GROUPTICKET DELETE", "ATTRACTION CREATE");

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>  (field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "HELP",
            "EXIT");

    private final ParkController park;
    private final UserService userService;
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
        this.userService = park.users();
    }

    /**
     * Processes one command line.
     *
     * @return the response to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        try {
            String response = dispatch(parts);
            if (STATE_CHANGING.contains(parts[0])) {
                park.saveState();
            }
            return response;
        } catch (OperationException e) {
            return error(e.getMessage());
        } catch (IOException e) {
            return error("Could not save park data: " + e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected error processing command");
        }
    }

    private String dispatch(String[] parts) throws OperationException {
        return switch (parts[0]) {
            case "USER CREATE" -> userCreate(parts);
            case "USER DELETE" -> userDelete(parts);
            case "USER UPDATE" -> userUpdate(parts);
            case "SYSTEM DATE" -> systemDate(parts);
            case "SYSTEM PRICE" -> systemPrice(parts);
            case "GROUP CREATE" -> groupCreate(parts);
            case "GROUP ADD" -> groupAdd(parts);
            case "GROUP REMOVE" -> groupRemove(parts);
            case "GROUP DELETE" -> groupDelete(parts);
            case "TICKET CREATE" -> ticketCreate(parts);
            case "TICKET DELETE" -> ticketDelete(parts);
            case "GROUPTICKET CREATE" -> groupTicketCreate(parts);
            case "GROUPTICKET DELETE" -> groupTicketDelete(parts);
            case "ATTRACTION CREATE" -> attractionCreate(parts);
            case "ATTRACTION ENTER" -> attractionEnter(parts);
            case "ATTRACTION EXIT" -> attractionExit(parts);
            case "ATTRACTION LIST" -> attractionList(parts);
            case "ATTRACTION STATUS" -> attractionStatus(parts);
            case "HELP" -> help(parts);
            case "EXIT" -> exit(parts);
            default -> error("Unknown command");
        };
    }

    /**
     * Tells whether an {@code EXIT} command has been processed; no further commands must run.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String groupCreate(String[] parts) throws OperationException {
        requireArguments(parts, 2, "GROUP CREATE");
        park.groups().createGroup(parts[1], parts[2]);
        return "GROUP CREATED";
    }

    private String groupAdd(String[] parts) throws OperationException {
        requireArguments(parts, 2, "GROUP ADD");
        park.groups().addMember(parts[1], parts[2]);
        return "USER ADDED TO GROUP";
    }

    private String groupRemove(String[] parts) throws OperationException {
        requireArguments(parts, 2, "GROUP REMOVE");
        park.groups().removeMember(parts[1], parts[2]);
        return "USER REMOVED FROM GROUP";
    }

    private String groupDelete(String[] parts) throws OperationException {
        requireArguments(parts, 1, "GROUP DELETE");
        park.deleteGroup(parts[1]);
        return "GROUP DELETED";
    }

    private String ticketCreate(String[] parts) throws OperationException {
        requireArguments(parts, 3, "TICKET CREATE");
        Ticket ticket = park.tickets().createIndividualTicket(parts[1], parts[2], parseDate(parts[3]));
        return "TICKET CREATED. Price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    private String ticketDelete(String[] parts) throws OperationException {
        requireArguments(parts, 1, "TICKET DELETE");
        park.tickets().deleteIndividualTicket(parts[1]);
        return "TICKET DELETED";
    }

    private String groupTicketCreate(String[] parts) throws OperationException {
        requireArguments(parts, 3, "GROUPTICKET CREATE");
        Ticket ticket = park.createGroupTicket(parts[1], parts[2], parseDate(parts[3]));
        return "GROUPTICKET CREATED. Total price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    private String groupTicketDelete(String[] parts) throws OperationException {
        requireArguments(parts, 1, "GROUPTICKET DELETE");
        park.tickets().deleteGroupTicket(parts[1]);
        return "GROUPTICKET DELETED";
    }

    private String attractionCreate(String[] parts) throws OperationException {
        requireArguments(parts, 3, "ATTRACTION CREATE");
        park.attractions().createAttraction(parts[1], parts[2], parseCapacity(parts[3]));
        return "ATTRACTION CREATED";
    }

    private String attractionEnter(String[] parts) throws OperationException {
        requireArguments(parts, 2, "ATTRACTION ENTER");
        return park.enterAttraction(parts[1], parts[2]).stream()
                .map(u -> "USER ENTERED ATTRACTION: " + u.getEmail())
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private String attractionExit(String[] parts) throws OperationException {
        requireArguments(parts, 2, "ATTRACTION EXIT");
        return "USER LEFT ATTRACTION: " + park.attractions().exit(parts[1], parts[2]).getEmail();
    }

    private String attractionStatus(String[] parts) throws OperationException {
        requireArguments(parts, 1, "ATTRACTION STATUS");
        AttractionStatus s = park.attractions().status(parts[1]);
        return String.join(System.lineSeparator(),
                "Attraction: " + s.id(),
                "Name: " + s.name(),
                "Capacity: " + s.capacity(),
                "Occupants: " + s.occupants(),
                "Available: " + s.available(),
                "Users: " + (s.occupantEmails().isEmpty() ? "none" : String.join(", ", s.occupantEmails())));
    }

    private String attractionList(String[] parts) throws OperationException {
        requireArguments(parts, 1, "ATTRACTION LIST");
        List<String> emails = park.attractions().listOccupantEmails(parts[1]);
        return emails.isEmpty() ? "NO USERS IN ATTRACTION " + parts[1] : String.join(System.lineSeparator(), emails);
    }

    private static int parseCapacity(String text) throws OperationException {
        if (text.isEmpty() || text.length() > 9 || !digits(text)) {
            throw new OperationException("Capacity must be a whole number of at least 1: " + text);
        }
        return Integer.parseInt(text);
    }

    private String systemDate(String[] parts) throws OperationException {
        requireArguments(parts, 1, "SYSTEM DATE");
        park.setCurrentDate(parseDate(parts[1]));
        return "SYSTEM DATE UPDATED";
    }

    private static LocalDate parseDate(String text) throws OperationException {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new OperationException("Invalid date, expected yyyy-MM-dd: " + text);
        }
    }

    private String systemPrice(String[] parts) throws OperationException {
        requireArguments(parts, 1, "SYSTEM PRICE");
        BigDecimal price = parsePrice(parts[1]);
        if (price == null) {
            throw new OperationException("Invalid price, expected a positive amount: " + parts[1]);
        }
        park.setBasePrice(price);
        return "SYSTEM PRICE UPDATED";
    }

    /**
     * Parses a positive amount with at most two decimals, or returns {@code null}.
     */
    static BigDecimal parsePrice(String text) {
        int dot = text.indexOf('.');
        String whole = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (whole.isEmpty() || !digits(whole) || !digits(decimals) || decimals.length() > PRICE_DECIMALS
                || (dot >= 0 && decimals.isEmpty())) {
            return null;
        }
        BigDecimal price = new BigDecimal(text).setScale(PRICE_DECIMALS);
        return price.signum() > 0 ? price : null;
    }

    private static boolean digits(String text) {
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    private String help(String[] parts) throws OperationException {
        requireArguments(parts, 0, "HELP");
        return HELP_TEXT;
    }

    private String exit(String[] parts) throws OperationException {
        requireArguments(parts, 0, "EXIT");
        exitRequested = true;
        return "Goodbye";
    }

    private String userCreate(String[] parts) throws OperationException {
        requireArguments(parts, 7, "USER CREATE");
        userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "USER CREATED";
    }

    private String userDelete(String[] parts) throws OperationException {
        requireArguments(parts, 2, "USER DELETE");
        park.deleteUser(parts[1], parts[2]);
        return "USER DELETED";
    }

    private String userUpdate(String[] parts) throws OperationException {
        requireArguments(parts, 4, "USER UPDATE");
        UserField field = UserField.fromName(parts[3])
                .orElseThrow(() -> new OperationException("Field cannot be modified: " + parts[3]));
        userService.updateUser(parts[1], parts[2], field, parts[4]);
        return "USER UPDATED";
    }

    private static void requireArguments(String[] parts, int expected, String command)
            throws OperationException {
        if (parts.length - 1 != expected) {
            throw new OperationException("Wrong number of arguments for " + command);
        }
    }

    private static String error(String reason) {
        return "ERROR: " + reason;
    }
}
