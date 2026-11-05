package es.upm.amusementpark.presentation;

import es.upm.amusementpark.persistence.storage.PersistenceException;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.OperationException;

import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.control.Park;
import es.upm.amusementpark.business.service.AttractionService;
import es.upm.amusementpark.business.service.GroupService;
import es.upm.amusementpark.business.service.InvoiceService;
import es.upm.amusementpark.business.service.PassService;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.service.TicketService;
import es.upm.amusementpark.business.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import es.upm.amusementpark.presentation.help.CommandInfo;
import es.upm.amusementpark.presentation.help.HelpGenerator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets text commands whose elements are separated by ';'.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    /** Handler of a command, receiving all elements (element 0 is the command name). */
    @FunctionalInterface
    private interface Handler {
        String handle(String[] elements);
    }

    private record Command(int minElements, int maxElements, String usage, String note, String purpose, Handler handler) {
    }

    /** Brief purpose of each command, shown by HELP EXTENDED. */
    private static final Map<String, String> PURPOSES = Map.ofEntries(
            Map.entry("USER CREATE", "Registers a new user after validating its data and checking DNI/email uniqueness."),
            Map.entry("USER DELETE", "Deletes a user after checking the password, if no active ticket or locked group prevents it."),
            Map.entry("USER UPDATE", "Changes one modifiable field of a user after checking the password; the DNI cannot change."),
            Map.entry("SYSTEM DATE", "Sets the current system date used by all date-dependent operations."),
            Map.entry("SYSTEM PRICE", "Sets the base ticket price used for tickets created from now on (this execution only)."),
            Map.entry("GROUP CREATE", "Creates an empty group with a unique identifier and a name."),
            Map.entry("GROUP ADD", "Adds a registered user to a group that is not locked by an active group ticket."),
            Map.entry("GROUP REMOVE", "Removes a member from a group that is not locked by an active group ticket."),
            Map.entry("GROUP DELETE", "Deletes a group without active group tickets; historical tickets are kept."),
            Map.entry("TICKET CREATE", "Buys an individual ticket for a user and date at the current price and shows the price."),
            Map.entry("TICKET DELETE", "Deletes a future individual ticket that is not part of a group ticket."),
            Map.entry("GROUPTICKET CREATE", "Buys a discounted group ticket for all current members of a group and shows the total."),
            Map.entry("GROUPTICKET DELETE", "Deletes a future group ticket together with its individual tickets."),
            Map.entry("SPEEDPASS CREATE", "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket."),
            Map.entry("SPEEDPASS DELETE", "Refunds an unconsumed SpeedPass of a ticket dated after the current date."),
            Map.entry("SPECIALPASS CREATE", "Buys the single Special Pass (ticket price) allowed for an individual ticket."),
            Map.entry("SPECIALPASS DELETE", "Refunds an unconsumed Special Pass of a ticket dated after the current date."),
            Map.entry("ATTRACTION CREATE", "Creates an attraction with a unique identifier, a name and a maximum capacity."),
            Map.entry("ATTRACTION ENTER", "Lets the users of a ticket valid today into an attraction, all together or none."),
            Map.entry("ATTRACTION EXIT", "Takes a user out of an attraction, freeing one place."),
            Map.entry("ATTRACTION LIST", "Lists the emails of the users currently inside an attraction."),
            Map.entry("ATTRACTION STATUS", "Shows capacity, occupancy, free places and users of an attraction."),
            Map.entry("INVOICE CREATE", "Generates the invoice of a user or a group from the purchases in the system."),
            Map.entry("HELP", "Shows the syntax of the available commands."),
            Map.entry("HELP EXTENDED", "Shows the syntax and purpose of the available commands."),
            Map.entry("EXIT", "Ends the application; no further command is executed."));

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final UserService userService;
    private boolean exitRequested;

    public CommandProcessor(UserService userService, GroupService groupService, TicketService ticketService,
                            AttractionService attractionService, InvoiceService invoiceService, PassService passService,
                            HelpGenerator help, HelpGenerator extendedHelp) {
        this.userService = userService;
        register("USER CREATE", 8, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", "",
                p -> {
                    userService.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    return "USER CREATED";
                });
        register("USER DELETE", 3, "USER DELETE;<Email>;<Password>", "", p -> {
            userService.deleteUser(p[1], p[2]);
            return "USER DELETED";
        });
        register("USER UPDATE", 5, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "(Field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)", this::userUpdate);
        register("SYSTEM DATE", 2, "SYSTEM DATE;<yyyy-MM-dd>", "", this::systemDate);
        register("SYSTEM PRICE", 2, "SYSTEM PRICE;<Price>", "", this::systemPrice);
        register("GROUP CREATE", 3, "GROUP CREATE;<Identifier>;<Name>", "", p -> {
            groupService.createGroup(p[1], p[2]);
            return "GROUP CREATED";
        });
        register("GROUP ADD", 3, "GROUP ADD;<Identifier>;<Email>", "", p -> {
            groupService.addMember(p[1], p[2]);
            return "USER ADDED TO GROUP";
        });
        register("GROUP REMOVE", 3, "GROUP REMOVE;<Identifier>;<Email>", "", p -> {
            groupService.removeMember(p[1], p[2]);
            return "USER REMOVED FROM GROUP";
        });
        register("GROUP DELETE", 2, "GROUP DELETE;<Identifier>", "", p -> {
            groupService.deleteGroup(p[1]);
            return "GROUP DELETED";
        });
        register("TICKET CREATE", 4, "TICKET CREATE;<identifier>;<email>;<date>", "(date: yyyy-MM-dd)", p ->
                "TICKET CREATED. Price: " + ticketService.createTicket(p[1], p[2], parseDate(p[3])).formattedPrice());
        register("TICKET DELETE", 2, "TICKET DELETE;<identifier>", "", p -> {
            ticketService.deleteTicket(p[1]);
            return "TICKET DELETED";
        });
        register("GROUPTICKET CREATE", 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>", "(date: yyyy-MM-dd)", p ->
                "GROUP TICKET CREATED. Total price: "
                        + ticketService.createGroupTicket(p[1], p[2], parseDate(p[3])).formattedPrice());
        register("GROUPTICKET DELETE", 2, "GROUPTICKET DELETE;<identifier>", "", p -> {
            ticketService.deleteGroupTicket(p[1]);
            return "GROUP TICKET DELETED";
        });
        register("SPEEDPASS CREATE", 4, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>", "(quantity: 1 or 10)",
                p -> {
                    var created = passService.createSpeedPasses(p[1], p[2], p[3]);
                    String ids = created.size() == 1 ? created.get(0).getIdentifier()
                            : created.get(0).getIdentifier() + ".." + created.get(created.size() - 1).getIdentifier();
                    return "SPEEDPASS CREATED: " + ids + ". Total price: " + money(PassService.total(created));
                });
        register("SPEEDPASS DELETE", 2, "SPEEDPASS DELETE;<speedPassIdentifier>", "", p -> {
            passService.refund(p[1], SpeedPass.class);
            return "SPEEDPASS DELETED";
        });
        register("SPECIALPASS CREATE", 3, "SPECIALPASS CREATE;<identifier>;<individualTicket>", "", p ->
                "SPECIALPASS CREATED. Price: " + money(passService.createSpecialPass(p[1], p[2]).getPrice()));
        register("SPECIALPASS DELETE", 2, "SPECIALPASS DELETE;<specialPassIdentifier>", "", p -> {
            passService.refund(p[1], SpecialPass.class);
            return "SPECIALPASS DELETED";
        });
        register("ATTRACTION CREATE", 4, 5, "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                "(type: GENERAL, INDIVIDUAL, GROUP, SPECIAL; optional, default GENERAL)", p -> {
            attractionService.createAttraction(p[1], p[2], p[3], p.length == 5 ? p[4] : "GENERAL");
            return "ATTRACTION CREATED";
        });
        register("ATTRACTION ENTER", 3, "ATTRACTION ENTER;<attraction>;<ticket>", "", p ->
                "ENTRY GRANTED: " + attractionService.enter(p[1], p[2]) + " user(s) entered " + p[1]);
        register("ATTRACTION EXIT", 3, "ATTRACTION EXIT;<attraction>;<email>", "", p -> {
            attractionService.exit(p[1], p[2]);
            return "USER LEFT ATTRACTION";
        });
        register("ATTRACTION LIST", 2, "ATTRACTION LIST;<attraction>", "", p -> attractionService.list(p[1]));
        register("ATTRACTION STATUS", 2, "ATTRACTION STATUS;<attractionIdentifier>", "", p -> attractionService.status(p[1]));
        register("INVOICE CREATE", 3, "INVOICE CREATE;USER;<email>", "| INVOICE CREATE;GROUP;<groupIdentifier>",
                p -> invoiceService.create(p[1], p[2]).render());
        register("HELP", 1, "HELP", "", p -> help.generate(catalogue()));
        register("HELP EXTENDED", 1, "HELP EXTENDED", "", p -> extendedHelp.generate(catalogue()));
        register("EXIT", 1, "EXIT", "", p -> {
            exitRequested = true;
            return "GOODBYE";
        });
    }

    private void register(String name, int elements, String usage, String note, Handler handler) {
        register(name, elements, elements, usage, note, handler);
    }

    private void register(String name, int minElements, int maxElements, String usage, String note, Handler handler) {
        commands.put(name, new Command(minElements, maxElements, usage, note, PURPOSES.getOrDefault(name, ""), handler));
    }

    /** Whether an EXIT command has been processed; no further commands must run. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    /** Descriptions of every available command, in registration order. */
    public List<CommandInfo> catalogue() {
        return commands.values().stream().map(c -> new CommandInfo(c.usage(), c.note(), c.purpose())).toList();
    }

    /**
     * Processes one command line.
     *
     * @return the response to show, or an empty string when there is nothing to show
     */
    public String process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return "";
        }
        String[] parts = line.split(SEPARATOR, -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        Command command = commands.get(parts[0]);
        if (command == null) {
            return "ERROR: unknown command '" + parts[0] + "'";
        }
        if (parts.length < command.minElements() || parts.length > command.maxElements()) {
            return "ERROR: wrong number of arguments. Usage: " + command.usage();
        }
        try {
            return command.handler().handle(parts);
        } catch (OperationException e) {
            return "ERROR: " + e.getMessage();
        } catch (PersistenceException e) {
            return "ERROR: data could not be saved (" + e.getMessage() + ")";
        } catch (RuntimeException e) {
            return "ERROR: the command could not be processed (" + e.getMessage() + ")";
        }
    }

    private String userUpdate(String[] p) {
        if ("DNI".equals(p[3])) {
            return "ERROR: the DNI cannot be modified";
        }
        UserField field = UserField.fromName(p[3]).orElse(null);
        if (field == null) {
            return "ERROR: unknown field '" + p[3] + "'. Modifiable fields: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD";
        }
        userService.updateUser(p[1], p[2], field, p[4]);
        return "USER UPDATED";
    }

    private String systemDate(String[] p) {
        Park.getInstance().setCurrentDate(parseDate(p[1]));
        return "SYSTEM DATE UPDATED";
    }

    private String systemPrice(String[] p) {
        Park.getInstance().setCurrentPrice(parsePrice(p[1]));
        return "SYSTEM PRICE UPDATED";
    }

    private static String money(java.math.BigDecimal amount) {
        return amount.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() + " EUR";
    }

    /** Parses a date in yyyy-MM-dd format, rejecting non-existent dates. */
    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidDataException("invalid date '" + text + "', expected yyyy-MM-dd");
        }
    }

    /** Parses a positive monetary amount with at most two decimals. */
    static BigDecimal parsePrice(String text) {
        int dot = text.indexOf('.');
        boolean wellFormed = !text.isEmpty() && dot == text.lastIndexOf('.') && dot != 0 && dot != text.length() - 1
                && (dot < 0 || text.length() - dot - 1 <= 2);
        for (char c : text.toCharArray()) {
            wellFormed &= (c >= '0' && c <= '9') || c == '.';
        }
        if (!wellFormed || new BigDecimal(text).signum() <= 0) {
            throw new InvalidDataException("invalid price '" + text + "', expected a positive amount with up to two decimals");
        }
        return new BigDecimal(text);
    }
}
