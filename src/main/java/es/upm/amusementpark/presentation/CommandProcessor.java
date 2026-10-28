package es.upm.amusementpark.presentation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.service.AttractionStatus;
import es.upm.amusementpark.business.service.PassService;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.service.UserService;

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
            "GROUPTICKET CREATE", "GROUPTICKET DELETE", "ATTRACTION CREATE", "SPEEDPASS CREATE", "SPEEDPASS DELETE",
            "SPECIALPASS CREATE", "SPECIALPASS DELETE");


    private final ParkController park;
    private final UserService userService;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private boolean exitRequested;

    /**
     * Creates a processor with the given help generators for {@code HELP} and {@code HELP EXTENDED}.
     */
    public CommandProcessor(ParkController park, HelpGenerator help, HelpGenerator extendedHelp) {
        this.park = park;
        this.userService = park.users();
        this.help = help;
        this.extendedHelp = extendedHelp;
    }

    /**
     * Creates a processor with the default basic and extended help.
     */
    public CommandProcessor(ParkController park) {
        this(park, new HelpGenerator(CommandCatalog.all(), new BasicHelpFormat()),
                new HelpGenerator(CommandCatalog.all(), new ExtendedHelpFormat()));
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
        } catch (ParkException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("Unexpected error processing command");
        }
    }

    private String dispatch(String[] parts) throws ParkException {
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
            case "INVOICE CREATE" -> invoiceCreate(parts);
            case "SPEEDPASS CREATE" -> speedPassCreate(parts);
            case "SPEEDPASS DELETE" -> speedPassDelete(parts);
            case "SPECIALPASS CREATE" -> specialPassCreate(parts);
            case "SPECIALPASS DELETE" -> specialPassDelete(parts);
            case "HELP" -> help(parts, help, "HELP");
            case "HELP EXTENDED" -> help(parts, extendedHelp, "HELP EXTENDED");
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

    private String groupCreate(String[] parts) throws ParkException {
        requireArguments(parts, 2, "GROUP CREATE");
        park.groups().createGroup(parts[1], parts[2]);
        return "GROUP CREATED";
    }

    private String groupAdd(String[] parts) throws ParkException {
        requireArguments(parts, 2, "GROUP ADD");
        park.groups().addMember(parts[1], parts[2]);
        return "USER ADDED TO GROUP";
    }

    private String groupRemove(String[] parts) throws ParkException {
        requireArguments(parts, 2, "GROUP REMOVE");
        park.groups().removeMember(parts[1], parts[2]);
        return "USER REMOVED FROM GROUP";
    }

    private String groupDelete(String[] parts) throws ParkException {
        requireArguments(parts, 1, "GROUP DELETE");
        park.deleteGroup(parts[1]);
        return "GROUP DELETED";
    }

    private String ticketCreate(String[] parts) throws ParkException {
        requireArguments(parts, 3, "TICKET CREATE");
        Ticket ticket = park.tickets().createIndividualTicket(parts[1], parts[2], parseDate(parts[3]));
        return "TICKET CREATED. Price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    private String ticketDelete(String[] parts) throws ParkException {
        requireArguments(parts, 1, "TICKET DELETE");
        park.deleteIndividualTicket(parts[1]);
        return "TICKET DELETED";
    }

    private String groupTicketCreate(String[] parts) throws ParkException {
        requireArguments(parts, 3, "GROUPTICKET CREATE");
        Ticket ticket = park.createGroupTicket(parts[1], parts[2], parseDate(parts[3]));
        return "GROUPTICKET CREATED. Total price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    private String groupTicketDelete(String[] parts) throws ParkException {
        requireArguments(parts, 1, "GROUPTICKET DELETE");
        park.deleteGroupTicket(parts[1]);
        return "GROUPTICKET DELETED";
    }

    private String speedPassCreate(String[] parts) throws ParkException {
        requireArguments(parts, 3, "SPEEDPASS CREATE");
        int quantity = switch (parts[3]) {
            case "1" -> 1;
            case "10" -> 10;
            default -> throw new CommandSyntaxException("SpeedPass quantity must be 1 or 10: " + parts[3]);
        };
        var passes = park.passes().buySpeedPasses(parts[1], parts[2], quantity);
        return "SPEEDPASS CREATED. Total price: " + PassService.total(passes).toPlainString() + " EUR";
    }

    private String speedPassDelete(String[] parts) throws ParkException {
        requireArguments(parts, 1, "SPEEDPASS DELETE");
        park.passes().refundSpeedPass(parts[1]);
        return "SPEEDPASS DELETED";
    }

    private String specialPassCreate(String[] parts) throws ParkException {
        requireArguments(parts, 2, "SPECIALPASS CREATE");
        return "SPECIALPASS CREATED. Price: " + park.passes().buySpecialPass(parts[1], parts[2]).getPrice().toPlainString()
                + " EUR";
    }

    private String specialPassDelete(String[] parts) throws ParkException {
        requireArguments(parts, 1, "SPECIALPASS DELETE");
        park.passes().refundSpecialPass(parts[1]);
        return "SPECIALPASS DELETED";
    }

    private String invoiceCreate(String[] parts) throws ParkException {
        requireArguments(parts, 2, "INVOICE CREATE");
        return InvoiceFormatter.format(switch (parts[1]) {
            case "USER" -> park.invoices().userInvoice(parts[2]);
            case "GROUP" -> park.invoices().groupInvoice(parts[2]);
            default -> throw new CommandSyntaxException("Invoice type must be USER or GROUP: " + parts[1]);
        });
    }

    private String attractionCreate(String[] parts) throws ParkException {
        if (parts.length != 4 && parts.length != 5) {
            throw new CommandSyntaxException("Wrong number of arguments for ATTRACTION CREATE");
        }
        AttractionType type = parts.length == 4 ? AttractionType.GENERAL : AttractionType.fromName(parts[4])
                .orElseThrow(() -> new CommandSyntaxException("Attraction type must be GENERAL, INDIVIDUAL, GROUP or SPECIAL"));
        park.attractions().createAttraction(parts[1], parts[2], parseCapacity(parts[3]), type);
        return "ATTRACTION CREATED";
    }

    private String attractionEnter(String[] parts) throws ParkException {
        if (parts.length != 3 && parts.length != 4) {
            throw new CommandSyntaxException("Wrong number of arguments for ATTRACTION ENTER");
        }
        if (parts.length == 4 && !parts[3].equals("SPEED")) {
            throw new CommandSyntaxException("The only ATTRACTION ENTER option is SPEED: " + parts[3]);
        }
        EntryResult result = park.enterAttraction(parts[1], parts[2], parts.length == 4);
        List<String> lines = new ArrayList<>();
        result.users().forEach(u -> lines.add("USER ENTERED ATTRACTION: " + u.getEmail()));
        for (Pass pass : result.consumed()) {
            String kind = pass instanceof SpeedPass ? "SPEEDPASS" : "SPECIALPASS";
            lines.add(kind + " CONSUMED: " + pass.getId() + " (ticket " + pass.getTicket().getId() + ")");
        }
        return String.join(System.lineSeparator(), lines);
    }

    private String attractionExit(String[] parts) throws ParkException {
        requireArguments(parts, 2, "ATTRACTION EXIT");
        return "USER LEFT ATTRACTION: " + park.attractions().exit(parts[1], parts[2]).getEmail();
    }

    private String attractionStatus(String[] parts) throws ParkException {
        requireArguments(parts, 1, "ATTRACTION STATUS");
        AttractionStatus s = park.attractions().status(parts[1]);
        return String.join(System.lineSeparator(),
                "Attraction: " + s.id(),
                "Name: " + s.name(),
                "Type: " + s.type(),
                "Capacity: " + s.capacity(),
                "Occupants: " + s.occupants(),
                "Available: " + s.available(),
                "Users: " + (s.occupantEmails().isEmpty() ? "none" : String.join(", ", s.occupantEmails())));
    }

    private String attractionList(String[] parts) throws ParkException {
        requireArguments(parts, 1, "ATTRACTION LIST");
        List<String> emails = park.attractions().listOccupantEmails(parts[1]);
        return emails.isEmpty() ? "NO USERS IN ATTRACTION " + parts[1] : String.join(System.lineSeparator(), emails);
    }

    static int parseCapacity(String text) throws ParkException {
        if (text.isEmpty() || text.length() > 9 || !digits(text)) {
            throw new CommandSyntaxException("Capacity must be a whole number of at least 1: " + text);
        }
        return Integer.parseInt(text);
    }

    private String systemDate(String[] parts) throws ParkException {
        requireArguments(parts, 1, "SYSTEM DATE");
        park.setCurrentDate(parseDate(parts[1]));
        return "SYSTEM DATE UPDATED";
    }

    static LocalDate parseDate(String text) throws ParkException {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new CommandSyntaxException("Invalid date, expected yyyy-MM-dd: " + text);
        }
    }

    private String systemPrice(String[] parts) throws ParkException {
        requireArguments(parts, 1, "SYSTEM PRICE");
        BigDecimal price = parsePrice(parts[1]);
        if (price == null) {
            throw new CommandSyntaxException("Invalid price, expected a positive amount: " + parts[1]);
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

    private String help(String[] parts, HelpGenerator generator, String command) throws ParkException {
        requireArguments(parts, 0, command);
        return generator.generate();
    }

    private String exit(String[] parts) throws ParkException {
        requireArguments(parts, 0, "EXIT");
        exitRequested = true;
        return "Goodbye";
    }

    private String userCreate(String[] parts) throws ParkException {
        requireArguments(parts, 7, "USER CREATE");
        userService.createUser(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
        return "USER CREATED";
    }

    private String userDelete(String[] parts) throws ParkException {
        requireArguments(parts, 2, "USER DELETE");
        park.deleteUser(parts[1], parts[2]);
        return "USER DELETED";
    }

    private String userUpdate(String[] parts) throws ParkException {
        requireArguments(parts, 4, "USER UPDATE");
        UserField field = UserField.fromName(parts[3])
                .orElseThrow(() -> new CommandSyntaxException("Field cannot be modified: " + parts[3]));
        userService.updateUser(parts[1], parts[2], field, parts[4]);
        return "USER UPDATED";
    }

    private static void requireArguments(String[] parts, int expected, String command)
            throws ParkException {
        if (parts.length - 1 != expected) {
            throw new CommandSyntaxException("Wrong number of arguments for " + command);
        }
    }

    private static String error(String reason) {
        return "ERROR: " + reason;
    }
}
