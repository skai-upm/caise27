package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.PersistenceException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.EntryResult;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.InvoiceService;
import es.upm.amusementpark.business.service.ParkController;
import es.upm.amusementpark.business.service.PassService;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;
import es.upm.amusementpark.presentation.help.ExtendedHelpFormatter;
import es.upm.amusementpark.presentation.help.HelpGenerator;
import es.upm.amusementpark.presentation.help.SyntaxHelpFormatter;

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

    private static final String USER_CREATE_SYNTAX =
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>";

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private static final String USER_DELETE_SYNTAX = "USER DELETE;<Email>;<Password>";

    private static final String USER_UPDATE_SYNTAX = "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>";

    private final PrintStream out;
    private final UserService userService;
    private final GroupService groupService;
    private final TicketService ticketService;
    private final AttractionService attractionService;
    private final InvoiceService invoiceService;
    private final PassService passService;

    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;

    /**
     * Creates a processor that delegates to the application's controllers,
     * with the default syntax and extended help formats.
     */
    public CommandProcessor(PrintStream out) {
        this(out, new HelpGenerator(new SyntaxHelpFormatter()), new HelpGenerator(new ExtendedHelpFormatter()));
    }

    /** Creates a processor whose HELP and HELP EXTENDED output is built by the given generators. */
    public CommandProcessor(PrintStream out, HelpGenerator help, HelpGenerator extendedHelp) {
        this.out = out;
        this.help = help;
        this.extendedHelp = extendedHelp;
        this.userService = UserService.getInstance();
        this.groupService = GroupService.getInstance();
        this.ticketService = TicketService.getInstance();
        this.attractionService = AttractionService.getInstance();
        this.invoiceService = InvoiceService.getInstance();
        this.passService = PassService.getInstance();
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
                out.println(help.generate());
                return true;
            case "HELP EXTENDED":
                out.println(extendedHelp.generate());
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
                createAttraction(parts);
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
            case "SPEEDPASS CREATE":
                buySpeedPasses(parts);
                return true;
            case "SPEEDPASS DELETE":
                requireElements(parts, 2, "SPEEDPASS DELETE;<speedPassIdentifier>");
                passService.refund(parts[1], SpeedPass.class);
                out.println("SPEEDPASS REFUNDED");
                return true;
            case "SPECIALPASS CREATE":
                requireElements(parts, 3, "SPECIALPASS CREATE;<identifier>;<individualTicket>");
                SpecialPass special = passService.buySpecialPass(parts[1], parts[2]);
                out.println("SPECIAL PASS CREATED. PRICE: " + special.getPrice() + " EUR");
                return true;
            case "SPECIALPASS DELETE":
                requireElements(parts, 2, "SPECIALPASS DELETE;<specialPassIdentifier>");
                passService.refund(parts[1], SpecialPass.class);
                out.println("SPECIAL PASS REFUNDED");
                return true;
            case "INVOICE CREATE":
                createInvoice(parts);
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

    private void createAttraction(String[] parts) throws ParkException, InvalidCommandException {
        if (parts.length != 4 && parts.length != 5) {
            throw new InvalidCommandException("invalid syntax. Expected ATTRACTION CREATE;<identifier>;<name>;<capacity>"
                    + " or ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>");
        }
        AttractionType type = AttractionType.GENERAL;
        if (parts.length == 5) {
            type = AttractionType.fromName(parts[4]);
            if (type == null) {
                throw new InvalidCommandException("invalid attraction type " + parts[4]
                        + ". Expected one of " + Arrays.toString(AttractionType.values()) + ".");
            }
        }
        attractionService.createAttraction(parts[1], parts[2], parseCapacity(parts[3]), type);
        out.println("ATTRACTION CREATED");
    }

    private void enterAttraction(String[] parts) throws ParkException, InvalidCommandException {
        if (parts.length != 3 && parts.length != 4) {
            throw new InvalidCommandException("invalid syntax. Expected ATTRACTION ENTER;<attraction>;<ticket>"
                    + " or ATTRACTION ENTER;<attraction>;<ticket>;SPEED");
        }
        if (parts.length == 4 && !parts[3].equals("SPEED")) {
            throw new InvalidCommandException("invalid entry option " + parts[3] + ". Expected SPEED.");
        }
        EntryResult result = attractionService.enter(parts[1], parts[2], parts.length == 4);
        out.println("ENTERED ATTRACTION " + parts[1] + ": " + emails(result.entered()));
        for (Pass pass : result.consumedPasses()) {
            String kind = pass instanceof SpecialPass ? "SPECIAL PASS" : "SPEEDPASS";
            out.println(kind + " CONSUMED: " + pass.getIdentifier());
        }
    }

    private void attractionStatus(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 2, "ATTRACTION STATUS;<attractionIdentifier>");
        Attraction<?> attraction = attractionService.findAttraction(parts[1]);
        out.println("ATTRACTION " + attraction.getIdentifier() + " - " + attraction.getName());
        out.println("Capacity: " + attraction.getCapacity());
        out.println("Users inside: " + attraction.getOccupants().size());
        out.println("Available places: " + attraction.getAvailablePlaces());
        out.println("Inside: " + (attraction.getOccupants().isEmpty() ? "none" : emails(attraction.getOccupants())));
    }

    private void listAttraction(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 2, "ATTRACTION LIST;<attraction>");
        Attraction<?> attraction = attractionService.findAttraction(parts[1]);
        if (attraction.getOccupants().isEmpty()) {
            out.println("No users inside attraction " + attraction.getIdentifier() + ".");
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
        }
    }

    private void buySpeedPasses(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 4, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
        int quantity;
        switch (parts[3]) {
            case "1":
                quantity = 1;
                break;
            case "10":
                quantity = 10;
                break;
            default:
                throw new InvalidCommandException("invalid quantity " + parts[3] + ". It must be 1 or 10.");
        }
        List<SpeedPass> bought = passService.buySpeedPasses(parts[1], parts[2], quantity);
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (SpeedPass pass : bought) {
            total = total.add(pass.getPrice());
        }
        String range = bought.size() == 1 ? bought.get(0).getIdentifier()
                : bought.get(0).getIdentifier() + ".." + bought.get(bought.size() - 1).getIdentifier();
        out.println("SPEEDPASS CREATED: " + range + ". TOTAL PRICE: " + total + " EUR");
    }

    private void createInvoice(String[] parts) throws ParkException, InvalidCommandException {
        requireElements(parts, 3, "INVOICE CREATE;USER;<email> or INVOICE CREATE;GROUP;<groupIdentifier>");
        Invoice invoice;
        switch (parts[1]) {
            case "USER":
                invoice = invoiceService.createUserInvoice(parts[2]);
                break;
            case "GROUP":
                invoice = invoiceService.createGroupInvoice(parts[2]);
                break;
            default:
                throw new InvalidCommandException("invalid invoice type " + parts[1] + ". Expected USER or GROUP.");
        }
        for (String line : invoice.render()) {
            out.println(line);
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
