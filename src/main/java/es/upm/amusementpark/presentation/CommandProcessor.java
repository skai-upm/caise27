package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.ParkController;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Processes single command lines. Every input source uses this class, so a
 * command behaves the same regardless of where it comes from. Command
 * elements are separated by ';'.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "  USER DELETE;<Email>;<Password>",
            "  USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "  SYSTEM DATE;<yyyy-MM-dd>",
            "  SYSTEM PRICE;<Price>",
            "  GROUP CREATE;<Identifier>;<Name>",
            "  GROUP ADD;<Identifier>;<Email>",
            "  GROUP REMOVE;<Identifier>;<Email>",
            "  GROUP DELETE;<Identifier>",
            "  TICKET CREATE;<identifier>;<email>;<date>",
            "  TICKET DELETE;<identifier>",
            "  GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "  GROUPTICKET DELETE;<identifier>",
            "  ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "  ATTRACTION ENTER;<attraction>;<ticket>",
            "  ATTRACTION EXIT;<attraction>;<email>",
            "  ATTRACTION LIST;<attraction>",
            "  ATTRACTION STATUS;<attractionIdentifier>",
            "  HELP",
            "  EXIT");

    private static final String USER_CREATE_SYNTAX =
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private static final String USER_DELETE_SYNTAX = "USER DELETE;<Email>;<Password>";

    private static final String USER_UPDATE_SYNTAX = "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>";

    private final PrintStream out;
    private final UserService userService;
    private final GroupService groupService = new GroupService();
    private final TicketService ticketService = new TicketService();
    private final AttractionService attractionService = new AttractionService();

    /** Creates a processor working on the users managed by the {@link ParkController}. */
    public CommandProcessor(PrintStream out) {
        this(out, new UserService(ParkController.getInstance().getUsers()));
    }

    public CommandProcessor(PrintStream out, UserService userService) {
        this.out = out;
        this.userService = userService;
    }

    /**
     * Processes one command line. Rejected and malformed commands are reported
     * as errors; they never stop the processing of later commands.
     *
     * @return {@code false} when the application must stop processing commands
     */
    public boolean process(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        try {
            boolean keepRunning = dispatch(command);
            ParkController.getInstance().persistAll();
            return keepRunning;
        } catch (ParkException | InvalidCommandException | PersistenceException e) {
            out.println("ERROR: " + e.getMessage());
            return true;
        } catch (RuntimeException e) {
            out.println("ERROR: unexpected error processing command: " + e.getMessage());
            return true;
        }
    }

    private boolean dispatch(String command) throws ParkException, InvalidCommandException {
        String[] parts = command.split(";", -1);
        switch (parts[0]) {
            case "HELP":
                out.println(HELP_TEXT);
                return true;
            case "EXIT":
                out.println("Exiting application.");
                return false;
            case "USER CREATE":
                requireElements(parts, 8, USER_CREATE_SYNTAX);
                userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                out.println("USER CREATED");
                return true;
            case "USER DELETE":
                requireElements(parts, 3, USER_DELETE_SYNTAX);
                userService.deleteUser(parts[1], parts[2]);
                out.println("USER DELETED");
                return true;
            case "USER UPDATE":
                requireElements(parts, 5, USER_UPDATE_SYNTAX);
                userService.updateUser(parts[1], parts[2], parseField(parts[3]), parts[4]);
                out.println("USER UPDATED");
                return true;
            case "SYSTEM DATE":
                setSystemDate(parts);
                return true;
            case "SYSTEM PRICE":
                setSystemPrice(parts);
                return true;
            case "GROUP CREATE":
                requireElements(parts, 3, "GROUP CREATE;<Identifier>;<Name>");
                groupService.createGroup(parts[1], parts[2]);
                out.println("GROUP CREATED");
                return true;
            case "GROUP ADD":
                requireElements(parts, 3, "GROUP ADD;<Identifier>;<Email>");
                groupService.addUser(parts[1], parts[2]);
                out.println("USER ADDED TO GROUP");
                return true;
            case "GROUP REMOVE":
                requireElements(parts, 3, "GROUP REMOVE;<Identifier>;<Email>");
                groupService.removeUser(parts[1], parts[2]);
                out.println("USER REMOVED FROM GROUP");
                return true;
            case "GROUP DELETE":
                requireElements(parts, 2, "GROUP DELETE;<Identifier>");
                groupService.deleteGroup(parts[1]);
                out.println("GROUP DELETED");
                return true;
            case "TICKET CREATE":
                createTicket(parts);
                return true;
            case "TICKET DELETE":
                requireElements(parts, 2, "TICKET DELETE;<identifier>");
                ticketService.deleteTicket(parts[1]);
                out.println("TICKET DELETED");
                return true;
            case "GROUPTICKET CREATE":
                createGroupTicket(parts);
                return true;
            case "GROUPTICKET DELETE":
                requireElements(parts, 2, "GROUPTICKET DELETE;<identifier>");
                ticketService.deleteGroupTicket(parts[1]);
                out.println("GROUP TICKET DELETED");
                return true;
            case "ATTRACTION CREATE":
                requireElements(parts, 4, "ATTRACTION CREATE;<identifier>;<name>;<capacity>");
                attractionService.createAttraction(parts[1], parts[2], parseCapacity(parts[3]));
                out.println("ATTRACTION CREATED");
                return true;
            case "ATTRACTION ENTER":
                enterAttraction(parts);
                return true;
            case "ATTRACTION EXIT":
                requireElements(parts, 3, "ATTRACTION EXIT;<attraction>;<email>");
                User user = attractionService.exit(parts[1], parts[2]);
                out.println("EXITED ATTRACTION " + parts[1] + ": " + user.getEmail());
                return true;
            case "ATTRACTION LIST":
                listAttraction(parts);
                return true;
            case "ATTRACTION STATUS":
                attractionStatus(parts);
                return true;
            default:
                throw new InvalidCommandException("unrecognised command: " + command);
        }
    }

    private static void requireElements(String[] parts, int elements, String syntax) throws InvalidCommandException {
        if (parts.length != elements) {
            throw new InvalidCommandException("invalid syntax. Expected " + syntax);
        }
    }

    private static UserField parseField(String text) throws InvalidCommandException {
        UserField field = UserField.fromName(text);
        if (field == null) {
            throw new InvalidCommandException("field " + text + " cannot be modified. Modifiable fields: "
                    + Arrays.toString(UserField.values()));
        }
        return field;
    }

    private void setSystemDate(String[] parts) throws InvalidCommandException {
        requireElements(parts, 2, "SYSTEM DATE;<yyyy-MM-dd>");
        LocalDate date = parseDate(parts[1]);
        ParkController.getInstance().setCurrentDate(date);
        out.println("SYSTEM DATE UPDATED: " + date);
    }

    private void setSystemPrice(String[] parts) throws InvalidCommandException {
        requireElements(parts, 2, "SYSTEM PRICE;<Price>");
        BigDecimal price = parsePrice(parts[1]);
        if (price == null || price.signum() <= 0) {
            throw new InvalidCommandException("invalid price " + parts[1]
                    + ". It must be a positive amount with at most two decimals.");
        }
        ParkController.getInstance().setCurrentPrice(price);
        out.println("SYSTEM PRICE UPDATED: " + ParkController.getInstance().getCurrentPrice());
    }

    /**
     * Parses a plain monetary amount (digits with an optional point and at
     * most two decimals).
     *
     * @return the amount, or {@code null} if the text is not such an amount
     */
    static BigDecimal parsePrice(String text) {
        int point = text.indexOf('.');
        String whole = point < 0 ? text : text.substring(0, point);
        String decimals = point < 0 ? "" : text.substring(point + 1);
        if (whole.isEmpty() || decimals.length() > 2 || (point >= 0 && decimals.isEmpty())) {
            return null;
        }
        for (char c : (whole + decimals).toCharArray()) {
            if (c < '0' || c > '9') {
                return null;
            }
        }
        return new BigDecimal(text);
    }

    private void createTicket(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 4, "TICKET CREATE;<identifier>;<email>;<date>");
        Ticket ticket = ticketService.createTicket(parts[1], parts[2], parseDate(parts[3]));
        out.println("TICKET CREATED. PRICE: " + ticket.getPrice() + " EUR");
    }

    private void createGroupTicket(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
        GroupTicket ticket = ticketService.createGroupTicket(parts[1], parts[2], parseDate(parts[3]));
        out.println("GROUP TICKET CREATED. TOTAL PRICE: " + ticket.getPrice() + " EUR");
    }

    private void enterAttraction(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 3, "ATTRACTION ENTER;<attraction>;<ticket>");
        List<User> entered = attractionService.enter(parts[1], parts[2]);
        out.println("ENTERED ATTRACTION " + parts[1] + ": " + emails(entered));
    }

    private void attractionStatus(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 2, "ATTRACTION STATUS;<attractionIdentifier>");
        Attraction attraction = attractionService.findAttraction(parts[1]);
        out.println("ATTRACTION " + attraction.getIdentifier() + " - " + attraction.getName());
        out.println("Capacity: " + attraction.getCapacity());
        out.println("Users inside: " + attraction.getOccupants().size());
        out.println("Available places: " + attraction.getAvailablePlaces());
        out.println("Inside: " + (attraction.getOccupants().isEmpty() ? "none" : emails(attraction.getOccupants())));
    }

    private void listAttraction(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 2, "ATTRACTION LIST;<attraction>");
        Attraction attraction = attractionService.findAttraction(parts[1]);
        if (attraction.getOccupants().isEmpty()) {
            out.println("No users inside attraction " + attraction.getIdentifier() + ".");
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
        }
    }

    private static String emails(List<User> users) {
        List<String> emails = new ArrayList<>();
        for (User user : users) {
            emails.add(user.getEmail());
        }
        return String.join(", ", emails);
    }

    /** Parses a capacity: a whole number of at least 1 that fits in an int. */
    private static int parseCapacity(String text) throws InvalidCommandException {
        boolean digits = !text.isEmpty() && text.length() <= 9;
        for (char c : text.toCharArray()) {
            digits &= c >= '0' && c <= '9';
        }
        if (!digits || Integer.parseInt(text) < 1) {
            throw new InvalidCommandException("invalid capacity " + text + ". It must be a positive whole number.");
        }
        return Integer.parseInt(text);
    }

    /** Parses a {@code yyyy-MM-dd} date. */
    private static LocalDate parseDate(String text) throws InvalidCommandException {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidCommandException("invalid date " + text + ". Expected format yyyy-MM-dd.");
        }
    }
}
