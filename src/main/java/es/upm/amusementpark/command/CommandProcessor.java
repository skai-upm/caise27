package es.upm.amusementpark.command;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.GroupTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.service.Park;
import es.upm.amusementpark.service.ParkException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interprets one {@code ;}-separated command line and executes it against the
 * {@link Park}. Parsing relies only on basic String operations and
 * {@code split}. Every command is registered once with its syntax, which is
 * also used to check the number of arguments and to build the HELP text.
 */
public class CommandProcessor {

    static final String USER_CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    static final String USER_DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";

    @FunctionalInterface
    private interface Handler {
        CommandResult execute(String[] arguments) throws ParkException;
    }

    private record Command(String syntax, int argumentCount, Handler handler) {
    }

    private final Park park;
    private final Map<String, Command> commands = new LinkedHashMap<>();

    public CommandProcessor(Park park) {
        this.park = park;
        registerUserCommands();
        registerSystemCommands();
        registerGroupCommands();
        registerTicketCommands();
        registerAttractionCommands();
        register("HELP", a -> CommandResult.of(helpText()));
        register("EXIT", a -> new CommandResult("Bye", true));
    }

    public CommandResult process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return CommandResult.of("");
        }
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        Command command = commands.get(parts[0].toUpperCase(Locale.ROOT));
        if (command == null) {
            return CommandResult.error("Unknown command '" + parts[0] + "'. Type HELP to list commands");
        }
        if (parts.length - 1 != command.argumentCount()) {
            return CommandResult.error("Wrong number of arguments. Usage: " + command.syntax());
        }
        try {
            return command.handler().execute(Arrays.copyOfRange(parts, 1, parts.length));
        } catch (ParkException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    private void registerUserCommands() {
        register(USER_CREATE_SYNTAX, a -> {
            park.createUser(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
            return CommandResult.of("USER CREATED");
        });
        register(USER_DELETE_SYNTAX, a -> {
            park.deleteUser(a[0], a[1]);
            return CommandResult.of("USER DELETED");
        });
        register(USER_UPDATE_SYNTAX, a -> {
            UserField field = UserField.fromString(a[2]);
            if (field == null) {
                return CommandResult.error("Field '" + a[2] + "' cannot be modified. Allowed fields: "
                        + UserField.allNames());
            }
            park.updateUser(a[0], a[1], field, a[3]);
            return CommandResult.of("USER UPDATED");
        });
    }

    private void registerSystemCommands() {
        register("SYSTEM DATE;<yyyy-MM-dd>", a -> {
            park.setDate(a[0]);
            return CommandResult.of("SYSTEM DATE SET TO " + park.getDate());
        });
        register("SYSTEM PRICE;<price>", a -> {
            park.setPrice(a[0]);
            return CommandResult.of("SYSTEM PRICE SET TO " + Park.formatAmount(park.getPrice()) + " EUR");
        });
    }

    private void registerGroupCommands() {
        register("GROUP CREATE;<identifier>;<name>", a -> {
            park.createGroup(a[0], a[1]);
            return CommandResult.of("GROUP CREATED");
        });
        register("GROUP ADD;<identifier>;<email>", a -> {
            park.addToGroup(a[0], a[1]);
            return CommandResult.of("USER ADDED TO GROUP");
        });
        register("GROUP REMOVE;<identifier>;<email>", a -> {
            park.removeFromGroup(a[0], a[1]);
            return CommandResult.of("USER REMOVED FROM GROUP");
        });
        register("GROUP DELETE;<identifier>", a -> {
            park.deleteGroup(a[0]);
            return CommandResult.of("GROUP DELETED");
        });
    }

    private void registerTicketCommands() {
        register("TICKET CREATE;<identifier>;<email>;<date>", a -> {
            Ticket ticket = park.createTicket(a[0], a[1], a[2]);
            return CommandResult.of("TICKET CREATED. Price: " + Park.formatAmount(ticket.getPrice()) + " EUR");
        });
        register("TICKET DELETE;<identifier>", a -> {
            park.deleteTicket(a[0]);
            return CommandResult.of("TICKET DELETED");
        });
        register("GROUPTICKET CREATE;<identifier>;<group>;<date>", a -> {
            GroupTicket ticket = park.createGroupTicket(a[0], a[1], a[2]);
            return CommandResult.of("GROUPTICKET CREATED. Tickets: " + ticket.getComposition().size()
                    + ", discount: " + ticket.getDiscountPercent() + "%, total: "
                    + Park.formatAmount(ticket.getPrice()) + " EUR");
        });
        register("GROUPTICKET DELETE;<identifier>", a -> {
            park.deleteGroupTicket(a[0]);
            return CommandResult.of("GROUPTICKET DELETED");
        });
    }

    private void registerAttractionCommands() {
        register("ATTRACTION CREATE;<identifier>;<name>;<capacity>", a -> {
            park.createAttraction(a[0], a[1], a[2]);
            return CommandResult.of("ATTRACTION CREATED");
        });
        register("ATTRACTION ENTER;<attraction>;<ticket>", a -> {
            int entered = park.enterAttraction(a[0], a[1]);
            return CommandResult.of("ATTRACTION ENTERED (" + entered + (entered == 1 ? " user)" : " users)"));
        });
        register("ATTRACTION EXIT;<attraction>;<email>", a -> {
            park.exitAttraction(a[0], a[1]);
            return CommandResult.of("ATTRACTION EXITED");
        });
        register("ATTRACTION LIST;<attraction>", a -> {
            Attraction attraction = park.requireAttraction(a[0]);
            if (attraction.getOccupants().isEmpty()) {
                return CommandResult.of("NO USERS IN ATTRACTION " + attraction.getId());
            }
            return CommandResult.of(String.join(System.lineSeparator(), emails(attraction)));
        });
        register("ATTRACTION STATUS;<attraction>",
                a -> CommandResult.of(status(park.requireAttraction(a[0]))));
    }

    private static String status(Attraction attraction) {
        List<String> emails = emails(attraction);
        return String.join(System.lineSeparator(),
                "Attraction: " + attraction.getId(),
                "Name: " + attraction.getName(),
                "Capacity: " + attraction.getCapacity(),
                "Users inside: " + emails.size(),
                "Free places: " + attraction.getFreePlaces(),
                "Users: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
    }

    private static List<String> emails(Attraction attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        return emails;
    }

    private void register(String syntax, Handler handler) {
        String[] elements = syntax.split(";", -1);
        commands.put(elements[0], new Command(syntax, elements.length - 1, handler));
    }

    private String helpText() {
        StringBuilder help = new StringBuilder("Available commands:");
        for (Command command : commands.values()) {
            help.append(System.lineSeparator()).append("  ").append(command.syntax());
            if (command.syntax().equals(USER_UPDATE_SYNTAX)) {
                help.append("   (field: ").append(UserField.allNames()).append(')');
            }
        }
        return help.toString();
    }
}
