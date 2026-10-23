package es.upm.park.cli;

import es.upm.park.model.Attraction;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.User;
import es.upm.park.service.AttractionService;
import es.upm.park.service.GroupService;
import es.upm.park.service.OperationException;
import es.upm.park.service.Park;
import es.upm.park.service.TicketService;
import es.upm.park.validation.ValueParser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets ';'-separated text commands using basic String operations and
 * dispatches them to the park services. The same instance serves both
 * interactive and file input. HELP is generated from the command registry.
 */
public class CommandProcessor {

    static final String SEPARATOR = ";";
    static final String EXIT_COMMAND = "EXIT";
    static final String HELP_COMMAND = "HELP";

    @FunctionalInterface
    private interface Handler {
        String handle(String[] fields) throws OperationException;
    }

    private record CommandSpec(String syntax, int fieldCount, Handler handler) {
    }

    private final Park park;
    private final GroupService groupService;
    private final TicketService ticketService;
    private final AttractionService attractionService;
    private final Map<String, CommandSpec> commands = new LinkedHashMap<>();

    public CommandProcessor(Park park) {
        this.park = park;
        this.groupService = new GroupService(park);
        this.ticketService = new TicketService(park);
        this.attractionService = new AttractionService(park);
        registerCommands();
    }

    private void registerCommands() {
        register("USER CREATE", "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", 8,
                f -> {
                    park.users().create(f[1], f[2], f[3], f[4], f[5], f[6], f[7]);
                    return "USER CREATED";
                });
        register("USER DELETE", "USER DELETE;<Email>;<Password>", 3, f -> {
            park.deleteUser(f[1], f[2]);
            return "USER DELETED";
        });
        register("USER UPDATE", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>", 5, f -> {
            park.users().update(f[1], f[2], f[3], f[4]);
            return "USER UPDATED";
        });
        register("SYSTEM DATE", "SYSTEM DATE;<yyyy-MM-dd>", 2, f -> {
            LocalDate date = ValueParser.parseDate(f[1]);
            park.setCurrentDate(date);
            return "SYSTEM DATE SET TO " + ValueParser.formatDate(date);
        });
        register("SYSTEM PRICE", "SYSTEM PRICE;<Price>", 2, f -> {
            BigDecimal price = ValueParser.parsePrice(f[1]);
            park.setCurrentPrice(price);
            return "SYSTEM PRICE SET TO " + price.toPlainString();
        });
        register("GROUP CREATE", "GROUP CREATE;<Identifier>;<Name>", 3, f -> {
            groupService.create(f[1], f[2]);
            return "GROUP CREATED";
        });
        register("GROUP ADD", "GROUP ADD;<Identifier>;<Email>", 3, f -> {
            groupService.addMember(f[1], f[2]);
            return "USER ADDED TO GROUP";
        });
        register("GROUP REMOVE", "GROUP REMOVE;<Identifier>;<Email>", 3, f -> {
            groupService.removeMember(f[1], f[2]);
            return "USER REMOVED FROM GROUP";
        });
        register("GROUP DELETE", "GROUP DELETE;<Identifier>", 2, f -> {
            groupService.delete(f[1]);
            return "GROUP DELETED";
        });
        register("TICKET CREATE", "TICKET CREATE;<identifier>;<email>;<date>", 4, f -> {
            IndividualTicket ticket = ticketService.createTicket(f[1], f[2], ValueParser.parseDate(f[3]));
            return "TICKET CREATED. PRICE: " + ticket.getPrice().toPlainString() + " EUR";
        });
        register("TICKET DELETE", "TICKET DELETE;<identifier>", 2, f -> {
            ticketService.deleteTicket(f[1]);
            return "TICKET DELETED";
        });
        register("GROUPTICKET CREATE", "GROUPTICKET CREATE;<identifier>;<group>;<date>", 4, f -> {
            GroupTicket ticket = ticketService.createGroupTicket(f[1], f[2], ValueParser.parseDate(f[3]));
            return "GROUP TICKET CREATED (" + ticket.getComposition().size() + " tickets). TOTAL PRICE: "
                    + ticket.getPrice().toPlainString() + " EUR";
        });
        register("GROUPTICKET DELETE", "GROUPTICKET DELETE;<identifier>", 2, f -> {
            ticketService.deleteGroupTicket(f[1]);
            return "GROUP TICKET DELETED";
        });
        register("ATTRACTION CREATE", "ATTRACTION CREATE;<identifier>;<name>;<capacity>", 4, f -> {
            attractionService.create(f[1], f[2], ValueParser.parseCapacity(f[3]));
            return "ATTRACTION CREATED";
        });
        register("ATTRACTION ENTER", "ATTRACTION ENTER;<attraction>;<ticket>", 3, f -> {
            List<User> entered = attractionService.enter(f[1], f[2]);
            return "ATTRACTION ENTERED: " + entered.size() + " user(s)";
        });
        register("ATTRACTION EXIT", "ATTRACTION EXIT;<attraction>;<email>", 3, f -> {
            attractionService.exit(f[1], f[2]);
            return "ATTRACTION EXITED";
        });
        register("ATTRACTION LIST", "ATTRACTION LIST;<attraction>", 2, f -> {
            Attraction attraction = attractionService.existingAttraction(f[1]);
            List<User> occupants = attraction.getOccupants();
            if (occupants.isEmpty()) {
                return "NO USERS IN ATTRACTION " + attraction.getId();
            }
            return String.join(System.lineSeparator(), emails(occupants));
        });
        register("ATTRACTION STATUS", "ATTRACTION STATUS;<attractionIdentifier>", 2,
                f -> formatStatus(attractionService.existingAttraction(f[1])));
        register(HELP_COMMAND, HELP_COMMAND, 1, f -> helpText());
        register(EXIT_COMMAND, EXIT_COMMAND, 1, f -> EXIT_COMMAND);
    }

    private void register(String name, String syntax, int fieldCount, Handler handler) {
        commands.put(name, new CommandSpec(syntax, fieldCount, handler));
    }

    public CommandResult process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return CommandResult.message("");
        }
        String[] fields = line.split(SEPARATOR, -1);
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }
        String name = normalizeCommandName(fields[0]);
        CommandSpec spec = commands.get(name);
        if (spec == null) {
            return CommandResult.error("Unknown command: " + fields[0] + ". Type HELP for the list of commands");
        }
        if (fields.length != spec.fieldCount()) {
            return CommandResult.error("Usage: " + spec.syntax());
        }
        try {
            String output = spec.handler().handle(fields);
            return EXIT_COMMAND.equals(name) ? CommandResult.exit(output) : CommandResult.message(output);
        } catch (OperationException e) {
            return CommandResult.error(e.getMessage());
        } catch (RuntimeException e) {
            return CommandResult.error("Unexpected problem processing the command: " + e.getMessage());
        }
    }

    public String helpText() {
        StringBuilder help = new StringBuilder("Available commands:");
        for (CommandSpec spec : commands.values()) {
            help.append(System.lineSeparator()).append("  ").append(spec.syntax());
        }
        help.append(System.lineSeparator()).append("  <Field> (USER UPDATE): NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD");
        return help.toString();
    }

    private static String formatStatus(Attraction attraction) {
        List<User> occupants = attraction.getOccupants();
        String users = occupants.isEmpty() ? "(none)" : String.join(", ", emails(occupants));
        return String.join(System.lineSeparator(),
                "ATTRACTION " + attraction.getId(),
                "  Name: " + attraction.getName(),
                "  Capacity: " + attraction.getCapacity(),
                "  Users inside: " + attraction.getOccupancy(),
                "  Available places: " + attraction.getAvailablePlaces(),
                "  Users: " + users);
    }

    private static List<String> emails(List<User> users) {
        return users.stream().map(User::getEmail).toList();
    }

    /**
     * Upper-cases the command name and collapses runs of whitespace into a
     * single space so that "user   create" is recognised as "USER CREATE".
     */
    static String normalizeCommandName(String raw) {
        StringBuilder normalized = new StringBuilder();
        boolean pendingSpace = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isWhitespace(c)) {
                pendingSpace = normalized.length() > 0;
            } else {
                if (pendingSpace) {
                    normalized.append(' ');
                    pendingSpace = false;
                }
                normalized.append(Character.toUpperCase(c));
            }
        }
        return normalized.toString();
    }
}
