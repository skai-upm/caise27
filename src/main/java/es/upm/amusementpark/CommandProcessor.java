package es.upm.amusementpark;

import java.io.PrintStream;
import java.io.UncheckedIOException;
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
     * Processes one command line.
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
        } catch (UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
            return true;
        } catch (RuntimeException e) {
            out.println("ERROR: unexpected error processing command: " + e.getMessage());
            return true;
        }
    }

    private boolean dispatch(String command) {
        String[] parts = command.split(";", -1);
        switch (parts[0]) {
            case "HELP":
                out.println(HELP_TEXT);
                return true;
            case "EXIT":
                out.println("Exiting application.");
                return false;
            case "USER CREATE":
                createUser(parts);
                return true;
            case "USER DELETE":
                deleteUser(parts);
                return true;
            case "USER UPDATE":
                updateUser(parts);
                return true;
            case "SYSTEM DATE":
                setSystemDate(parts);
                return true;
            case "SYSTEM PRICE":
                setSystemPrice(parts);
                return true;
            case "GROUP CREATE":
                groupCommand(parts, 3, "GROUP CREATE;<Identifier>;<Name>", "GROUP CREATED",
                        () -> groupService.createGroup(parts[1], parts[2]));
                return true;
            case "GROUP ADD":
                groupCommand(parts, 3, "GROUP ADD;<Identifier>;<Email>", "USER ADDED TO GROUP",
                        () -> groupService.addUser(parts[1], parts[2]));
                return true;
            case "GROUP REMOVE":
                groupCommand(parts, 3, "GROUP REMOVE;<Identifier>;<Email>", "USER REMOVED FROM GROUP",
                        () -> groupService.removeUser(parts[1], parts[2]));
                return true;
            case "GROUP DELETE":
                groupCommand(parts, 2, "GROUP DELETE;<Identifier>", "GROUP DELETED",
                        () -> groupService.deleteGroup(parts[1]));
                return true;
            case "TICKET CREATE":
                createTicket(parts);
                return true;
            case "TICKET DELETE":
                deleteTicket(parts);
                return true;
            case "GROUPTICKET CREATE":
                createGroupTicket(parts);
                return true;
            case "GROUPTICKET DELETE":
                deleteGroupTicket(parts);
                return true;
            case "ATTRACTION CREATE":
                createAttraction(parts);
                return true;
            case "ATTRACTION ENTER":
                enterAttraction(parts);
                return true;
            case "ATTRACTION EXIT":
                exitAttraction(parts);
                return true;
            case "ATTRACTION LIST":
                listAttraction(parts);
                return true;
            case "ATTRACTION STATUS":
                attractionStatus(parts);
                return true;
            default:
                out.println("ERROR: unrecognised command: " + command);
                return true;
        }
    }

    private void createUser(String[] parts) {
        if (parts.length != 8) {
            out.println("ERROR: invalid syntax. Expected " + USER_CREATE_SYNTAX);
            return;
        }
        try {
            userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
            out.println("USER CREATED");
        } catch (UserOperationException | UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void deleteUser(String[] parts) {
        if (parts.length != 3) {
            out.println("ERROR: invalid syntax. Expected " + USER_DELETE_SYNTAX);
            return;
        }
        try {
            userService.deleteUser(parts[1], parts[2]);
            out.println("USER DELETED");
        } catch (UserOperationException | UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void updateUser(String[] parts) {
        if (parts.length != 5) {
            out.println("ERROR: invalid syntax. Expected " + USER_UPDATE_SYNTAX);
            return;
        }
        UserField field = UserField.fromName(parts[3]);
        if (field == null) {
            out.println("ERROR: field " + parts[3] + " cannot be modified. Modifiable fields: "
                    + Arrays.toString(UserField.values()));
            return;
        }
        try {
            userService.updateUser(parts[1], parts[2], field, parts[4]);
            out.println("USER UPDATED");
        } catch (UserOperationException | UncheckedIOException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void setSystemDate(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR: invalid syntax. Expected SYSTEM DATE;<yyyy-MM-dd>");
            return;
        }
        LocalDate date = parseDate(parts[1]);
        if (date == null) {
            return;
        }
        ParkController.getInstance().setCurrentDate(date);
        out.println("SYSTEM DATE UPDATED: " + date);
    }

    private void setSystemPrice(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR: invalid syntax. Expected SYSTEM PRICE;<Price>");
            return;
        }
        BigDecimal price = parsePrice(parts[1]);
        if (price == null || price.signum() <= 0) {
            out.println("ERROR: invalid price " + parts[1] + ". It must be a positive amount with at most two decimals.");
            return;
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

    /** A group operation that may be rejected. */
    private interface GroupAction {
        void run() throws GroupOperationException;
    }

    private void groupCommand(String[] parts, int elements, String syntax, String success, GroupAction action) {
        if (parts.length != elements) {
            out.println("ERROR: invalid syntax. Expected " + syntax);
            return;
        }
        try {
            action.run();
            out.println(success);
        } catch (GroupOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void createTicket(String[] parts) {
        if (parts.length != 4) {
            out.println("ERROR: invalid syntax. Expected TICKET CREATE;<identifier>;<email>;<date>");
            return;
        }
        LocalDate date = parseDate(parts[3]);
        if (date == null) {
            return;
        }
        try {
            Ticket ticket = ticketService.createTicket(parts[1], parts[2], date);
            out.println("TICKET CREATED. PRICE: " + ticket.getPrice() + " EUR");
        } catch (TicketOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void deleteTicket(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR: invalid syntax. Expected TICKET DELETE;<identifier>");
            return;
        }
        try {
            ticketService.deleteTicket(parts[1]);
            out.println("TICKET DELETED");
        } catch (TicketOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void createGroupTicket(String[] parts) {
        if (parts.length != 4) {
            out.println("ERROR: invalid syntax. Expected GROUPTICKET CREATE;<identifier>;<group>;<date>");
            return;
        }
        LocalDate date = parseDate(parts[3]);
        if (date == null) {
            return;
        }
        try {
            GroupTicket ticket = ticketService.createGroupTicket(parts[1], parts[2], date);
            out.println("GROUP TICKET CREATED. TOTAL PRICE: " + ticket.getPrice() + " EUR");
        } catch (TicketOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void deleteGroupTicket(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR: invalid syntax. Expected GROUPTICKET DELETE;<identifier>");
            return;
        }
        try {
            ticketService.deleteGroupTicket(parts[1]);
            out.println("GROUP TICKET DELETED");
        } catch (TicketOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void createAttraction(String[] parts) {
        if (parts.length != 4) {
            out.println("ERROR: invalid syntax. Expected ATTRACTION CREATE;<identifier>;<name>;<capacity>");
            return;
        }
        int capacity = parseCapacity(parts[3]);
        if (capacity < 1) {
            out.println("ERROR: invalid capacity " + parts[3] + ". It must be a positive whole number.");
            return;
        }
        try {
            attractionService.createAttraction(parts[1], parts[2], capacity);
            out.println("ATTRACTION CREATED");
        } catch (AttractionOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void enterAttraction(String[] parts) {
        if (parts.length != 3) {
            out.println("ERROR: invalid syntax. Expected ATTRACTION ENTER;<attraction>;<ticket>");
            return;
        }
        try {
            List<User> entered = attractionService.enter(parts[1], parts[2]);
            List<String> emails = new ArrayList<>();
            for (User user : entered) {
                emails.add(user.getEmail());
            }
            out.println("ENTERED ATTRACTION " + parts[1] + ": " + String.join(", ", emails));
        } catch (AttractionOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void exitAttraction(String[] parts) {
        if (parts.length != 3) {
            out.println("ERROR: invalid syntax. Expected ATTRACTION EXIT;<attraction>;<email>");
            return;
        }
        try {
            User user = attractionService.exit(parts[1], parts[2]);
            out.println("EXITED ATTRACTION " + parts[1] + ": " + user.getEmail());
        } catch (AttractionOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void attractionStatus(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR: invalid syntax. Expected ATTRACTION STATUS;<attractionIdentifier>");
            return;
        }
        try {
            Attraction attraction = attractionService.findAttraction(parts[1]);
            List<String> emails = new ArrayList<>();
            for (User user : attraction.getOccupants()) {
                emails.add(user.getEmail());
            }
            out.println("ATTRACTION " + attraction.getIdentifier() + " - " + attraction.getName());
            out.println("Capacity: " + attraction.getCapacity());
            out.println("Users inside: " + attraction.getOccupants().size());
            out.println("Available places: " + attraction.getAvailablePlaces());
            out.println("Inside: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
        } catch (AttractionOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    private void listAttraction(String[] parts) {
        if (parts.length != 2) {
            out.println("ERROR: invalid syntax. Expected ATTRACTION LIST;<attraction>");
            return;
        }
        try {
            Attraction attraction = attractionService.findAttraction(parts[1]);
            if (attraction.getOccupants().isEmpty()) {
                out.println("No users inside attraction " + attraction.getIdentifier() + ".");
                return;
            }
            for (User user : attraction.getOccupants()) {
                out.println(user.getEmail());
            }
        } catch (AttractionOperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
    }

    /** @return the capacity, or -1 if the text is not a whole number that fits in an int */
    private static int parseCapacity(String text) {
        if (text.isEmpty() || text.length() > 9) {
            return -1;
        }
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return -1;
            }
        }
        return Integer.parseInt(text);
    }

    /**
     * Parses a {@code yyyy-MM-dd} date, printing an error if it is invalid.
     *
     * @return the date, or {@code null} if the text is not a valid date
     */
    private LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            out.println("ERROR: invalid date " + text + ". Expected format yyyy-MM-dd.");
            return null;
        }
    }
}
