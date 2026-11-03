package es.upm.etsisi.park;

import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parses and executes one command line. Shared by interactive and file mode.
 * Commands are registered with their parameter count and syntax; HELP is derived
 * from the registry.
 */
public class CommandProcessor {

    @FunctionalInterface
    private interface Handler {
        /** Executes the command and returns {@code false} if the application must stop. */
        boolean handle(String[] params) throws ValidationException;
    }

    private record Command(int parameters, String syntax, Handler handler) {
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final UserService users;
    private final SystemService system;
    private final GroupService groups;
    private final TicketService tickets;
    private final AttractionService attractions;
    private final PrintStream out;

    public CommandProcessor(Park park, PrintStream out) {
        this.users = new UserService(park);
        this.system = new SystemService(park);
        this.groups = new GroupService(park);
        this.tickets = new TicketService(park);
        this.attractions = new AttractionService(park);
        this.out = out;
        registerCommands();
    }

    private void registerCommands() {
        register("USER CREATE", 7, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", p -> {
            users.create(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
            return say("USER CREATED");
        });
        register("USER DELETE", 2, "USER DELETE;<Email>;<Password>", p -> {
            users.delete(p[0], p[1]);
            return say("USER DELETED");
        });
        register("USER UPDATE", 4, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>", p -> {
            users.update(p[0], p[1], p[2], p[3]);
            return say("USER UPDATED");
        });
        register("SYSTEM DATE", 1, "SYSTEM DATE;<yyyy-MM-dd>",
                p -> say("SYSTEM DATE SET TO " + system.setDate(p[0])));
        register("SYSTEM PRICE", 1, "SYSTEM PRICE;<Price>",
                p -> say("SYSTEM PRICE SET TO " + system.setPrice(p[0]).toPlainString()));
        register("GROUP CREATE", 2, "GROUP CREATE;<Identifier>;<Name>", p -> {
            groups.create(p[0], p[1]);
            return say("GROUP CREATED");
        });
        register("GROUP ADD", 2, "GROUP ADD;<Identifier>;<Email>", p -> {
            groups.addMember(p[0], p[1]);
            return say("USER ADDED TO GROUP");
        });
        register("GROUP REMOVE", 2, "GROUP REMOVE;<Identifier>;<Email>", p -> {
            groups.removeMember(p[0], p[1]);
            return say("USER REMOVED FROM GROUP");
        });
        register("GROUP DELETE", 1, "GROUP DELETE;<Identifier>", p -> {
            groups.delete(p[0]);
            return say("GROUP DELETED");
        });
        register("TICKET CREATE", 3, "TICKET CREATE;<identifier>;<email>;<date>",
                p -> say("TICKET CREATED. PRICE: " + tickets.createIndividual(p[0], p[1], p[2]).getPrice() + " EUR"));
        register("TICKET DELETE", 1, "TICKET DELETE;<identifier>", p -> {
            tickets.deleteIndividual(p[0]);
            return say("TICKET DELETED");
        });
        register("GROUPTICKET CREATE", 3, "GROUPTICKET CREATE;<identifier>;<group>;<date>",
                p -> say("GROUPTICKET CREATED. TOTAL PRICE: " + tickets.createGroup(p[0], p[1], p[2]).getPrice() + " EUR"));
        register("GROUPTICKET DELETE", 1, "GROUPTICKET DELETE;<identifier>", p -> {
            tickets.deleteGroup(p[0]);
            return say("GROUPTICKET DELETED");
        });
        register("ATTRACTION CREATE", 3, "ATTRACTION CREATE;<identifier>;<name>;<capacity>", p -> {
            attractions.create(p[0], p[1], p[2]);
            return say("ATTRACTION CREATED");
        });
        register("ATTRACTION ENTER", 2, "ATTRACTION ENTER;<attraction>;<ticket>",
                p -> say("ATTRACTION ENTERED: " + attractions.enter(p[0], p[1]) + " USER(S)"));
        register("ATTRACTION EXIT", 2, "ATTRACTION EXIT;<attraction>;<email>", p -> {
            attractions.exit(p[0], p[1]);
            return say("ATTRACTION EXITED");
        });
        register("ATTRACTION LIST", 1, "ATTRACTION LIST;<attraction>", p -> say(list(p[0])));
        register("ATTRACTION STATUS", 1, "ATTRACTION STATUS;<attractionIdentifier>", p -> say(status(p[0])));
        register("HELP", 0, "HELP", p -> say(help()));
        register("EXIT", 0, "EXIT", p -> {
            say("BYE");
            return false;
        });
    }

    private void register(String name, int parameters, String syntax, Handler handler) {
        commands.put(name, new Command(parameters, syntax, handler));
    }

    /**
     * Executes a command line.
     *
     * @return {@code false} when the command asks the application to stop
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        Command command = commands.get(parts[0]);
        if (command == null) {
            out.println("ERROR: unknown command '" + parts[0] + "'. Type HELP for the list of commands");
            return true;
        }
        if (parts.length - 1 != command.parameters()) {
            out.println("ERROR: wrong number of parameters. Usage: " + command.syntax());
            return true;
        }
        String[] params = new String[command.parameters()];
        System.arraycopy(parts, 1, params, 0, params.length);
        try {
            return command.handler().handle(params);
        } catch (ValidationException e) {
            out.println("ERROR: " + e.getMessage());
            return true;
        }
    }

    String help() {
        return commands.values().stream().map(Command::syntax).collect(Collectors.joining(System.lineSeparator()));
    }

    private String list(String attractionId) throws ValidationException {
        List<User> occupants = attractions.requireAttraction(attractionId).getOccupants();
        if (occupants.isEmpty()) {
            return "NO USERS INSIDE";
        }
        return occupants.stream().map(User::getEmail).collect(Collectors.joining(System.lineSeparator()));
    }

    private String status(String attractionId) throws ValidationException {
        Attraction a = attractions.requireAttraction(attractionId);
        List<User> occupants = a.getOccupants();
        String inside = occupants.isEmpty() ? "-"
                : occupants.stream().map(User::getEmail).collect(Collectors.joining(", "));
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + a.getId(),
                "NAME: " + a.getName(),
                "CAPACITY: " + a.getCapacity(),
                "INSIDE: " + occupants.size(),
                "AVAILABLE: " + a.getAvailablePlaces(),
                "USERS: " + inside);
    }

    private boolean say(String message) {
        out.println(message);
        return true;
    }
}
