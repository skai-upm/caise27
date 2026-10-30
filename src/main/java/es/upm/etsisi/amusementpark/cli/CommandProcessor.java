package es.upm.etsisi.amusementpark.cli;

import es.upm.etsisi.amusementpark.controller.AttractionController;
import es.upm.etsisi.amusementpark.controller.GroupController;
import es.upm.etsisi.amusementpark.controller.ParkController;
import es.upm.etsisi.amusementpark.controller.TicketController;
import es.upm.etsisi.amusementpark.controller.UserController;
import es.upm.etsisi.amusementpark.model.Attraction;
import es.upm.etsisi.amusementpark.model.User;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Interprets a single command line and executes it. Never throws for bad input.
 */
public class CommandProcessor {

    private static final String HELP = "HELP";
    private static final String EXIT = "EXIT";

    private record Command(int fieldCount, String usage, Function<String[], String> action) {
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final ParkController park;
    private final PrintStream out;

    public CommandProcessor(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
        registerUserCommands(new UserController(park));
        registerSystemCommands();
        registerGroupCommands(new GroupController(park));
        registerTicketCommands(new TicketController(park));
        registerAttractionCommands(new AttractionController(park));
    }

    /**
     * Processes one command line.
     *
     * @return false if the application must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.trim().split(";", -1);
        String name = parts[0].trim();
        try {
            if (name.equals(HELP) || name.equals(EXIT)) {
                requireArgs(parts, 1, name);
                out.println(name.equals(HELP) ? helpText() : "BYE");
                return name.equals(HELP);
            }
            Command command = commands.get(name);
            if (command == null) {
                out.println("ERROR: Unknown command: " + name + " (type HELP)");
                return true;
            }
            requireArgs(parts, command.fieldCount(), command.usage());
            out.println(command.action().apply(parts));
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR: Command could not be processed: " + e.getMessage());
        }
        return true;
    }

    String helpText() {
        List<String> lines = new ArrayList<>();
        lines.add("Available commands:");
        commands.values().forEach(c -> lines.add(c.usage()));
        lines.add("  fields for USER UPDATE: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD");
        lines.add(HELP);
        lines.add(EXIT);
        return String.join(System.lineSeparator(), lines);
    }

    private void register(String name, String usage, Function<String[], String> action) {
        commands.put(name, new Command(usage.split(";", -1).length, usage, action));
    }

    private void registerUserCommands(UserController users) {
        register("USER CREATE", "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", p -> {
            users.create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
            return "USER CREATED";
        });
        register("USER DELETE", "USER DELETE;<Email>;<Password>", p -> {
            users.delete(p[1], p[2]);
            return "USER DELETED";
        });
        register("USER UPDATE", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>", p -> {
            users.update(p[1], p[2], p[3], p[4]);
            return "USER UPDATED";
        });
    }

    private void registerSystemCommands() {
        register("SYSTEM DATE", "SYSTEM DATE;<yyyy-MM-dd>", p -> {
            park.setDate(p[1]);
            return "SYSTEM DATE SET TO " + park.today();
        });
        register("SYSTEM PRICE", "SYSTEM PRICE;<Price>", p -> {
            park.setPrice(p[1]);
            return "SYSTEM PRICE SET TO " + park.basePrice() + " EUR";
        });
    }

    private void registerGroupCommands(GroupController groups) {
        register("GROUP CREATE", "GROUP CREATE;<Identifier>;<Name>", p -> {
            groups.create(p[1], p[2]);
            return "GROUP CREATED";
        });
        register("GROUP ADD", "GROUP ADD;<Identifier>;<Email>", p -> {
            groups.addUser(p[1], p[2]);
            return "GROUP USER ADDED";
        });
        register("GROUP REMOVE", "GROUP REMOVE;<Identifier>;<Email>", p -> {
            groups.removeUser(p[1], p[2]);
            return "GROUP USER REMOVED";
        });
        register("GROUP DELETE", "GROUP DELETE;<Identifier>", p -> {
            groups.delete(p[1]);
            return "GROUP DELETED";
        });
    }

    private void registerTicketCommands(TicketController tickets) {
        register("TICKET CREATE", "TICKET CREATE;<identifier>;<email>;<date>",
                p -> "TICKET CREATED - PRICE: " + tickets.createTicket(p[1], p[2], p[3]).getPrice() + " EUR");
        register("TICKET DELETE", "TICKET DELETE;<identifier>", p -> {
            tickets.deleteTicket(p[1]);
            return "TICKET DELETED";
        });
        register("GROUPTICKET CREATE", "GROUPTICKET CREATE;<identifier>;<group>;<date>",
                p -> "GROUPTICKET CREATED - TOTAL PRICE: "
                        + tickets.createGroupTicket(p[1], p[2], p[3]).getPrice() + " EUR");
        register("GROUPTICKET DELETE", "GROUPTICKET DELETE;<identifier>", p -> {
            tickets.deleteGroupTicket(p[1]);
            return "GROUPTICKET DELETED";
        });
    }

    private void registerAttractionCommands(AttractionController attractions) {
        register("ATTRACTION CREATE", "ATTRACTION CREATE;<identifier>;<name>;<capacity>", p -> {
            attractions.create(p[1], p[2], p[3]);
            return "ATTRACTION CREATED";
        });
        register("ATTRACTION ENTER", "ATTRACTION ENTER;<attraction>;<ticket>",
                p -> "ATTRACTION ENTERED: " + attractions.enter(p[1], p[2]) + " user(s)");
        register("ATTRACTION EXIT", "ATTRACTION EXIT;<attraction>;<email>", p -> {
            attractions.exit(p[1], p[2]);
            return "ATTRACTION EXITED";
        });
        register("ATTRACTION LIST", "ATTRACTION LIST;<attraction>",
                p -> listOccupants(attractions.find(p[1])));
        register("ATTRACTION STATUS", "ATTRACTION STATUS;<attractionIdentifier>",
                p -> status(attractions.find(p[1])));
    }

    private static String listOccupants(Attraction attraction) {
        List<String> emails = attraction.getOccupants().stream().map(User::getEmail).toList();
        return emails.isEmpty() ? "NO USERS INSIDE" : String.join(System.lineSeparator(), emails);
    }

    private static String status(Attraction a) {
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + a.getId(),
                "NAME: " + a.getName(),
                "CAPACITY: " + a.getCapacity(),
                "INSIDE: " + a.getOccupants().size(),
                "AVAILABLE: " + a.getAvailablePlaces(),
                "USERS: " + String.join(", ", a.getOccupants().stream().map(User::getEmail).toList()));
    }

    private static void requireArgs(String[] parts, int expected, String usage) {
        if (parts.length != expected) {
            throw new IllegalArgumentException("Wrong number of arguments. Usage: " + usage);
        }
    }
}
