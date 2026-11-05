package es.upm.amusementpark.command;

import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.AttractionRepository;
import es.upm.amusementpark.service.AttractionService;
import es.upm.amusementpark.service.GroupService;
import es.upm.amusementpark.service.OperationException;
import es.upm.amusementpark.service.TicketService;
import es.upm.amusementpark.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
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

    private record Command(int elements, String usage, String note, Handler handler) {
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final UserService userService;
    private boolean exitRequested;

    public CommandProcessor(UserService userService) {
        this(userService, new GroupService(userService.users(), userService.groups(), userService.tickets()),
                new TicketService(userService.users(), userService.tickets(), userService.groups()));
    }

    public CommandProcessor(UserService userService, GroupService groupService, TicketService ticketService) {
        this(userService, groupService, ticketService,
                new AttractionService(new AttractionRepository(), userService.tickets()));
    }

    public CommandProcessor(UserService userService, GroupService groupService, TicketService ticketService,
                            AttractionService attractionService) {
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
        register("ATTRACTION CREATE", 4, "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "", p -> {
            attractionService.createAttraction(p[1], p[2], p[3]);
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
        register("HELP", 1, "HELP", "", p -> helpText());
        register("EXIT", 1, "EXIT", "", p -> {
            exitRequested = true;
            return "GOODBYE";
        });
    }

    private void register(String name, int elements, String usage, String note, Handler handler) {
        commands.put(name, new Command(elements, usage, note, handler));
    }

    /** Whether an EXIT command has been processed; no further commands must run. */
    public boolean isExitRequested() {
        return exitRequested;
    }

    /** Text listing every available command and its syntax. */
    public String helpText() {
        StringBuilder sb = new StringBuilder("Available commands:");
        for (Command c : commands.values()) {
            sb.append(System.lineSeparator()).append("  ").append(c.usage());
            if (!c.note().isEmpty()) {
                sb.append("   ").append(c.note());
            }
        }
        return sb.toString();
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
        if (parts.length != command.elements()) {
            return "ERROR: wrong number of arguments. Usage: " + command.usage();
        }
        try {
            return command.handler().handle(parts);
        } catch (OperationException e) {
            return "ERROR: " + e.getMessage();
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

    /** Parses a date in yyyy-MM-dd format, rejecting non-existent dates. */
    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new OperationException("invalid date '" + text + "', expected yyyy-MM-dd");
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
            throw new OperationException("invalid price '" + text + "', expected a positive amount with up to two decimals");
        }
        return new BigDecimal(text);
    }
}
