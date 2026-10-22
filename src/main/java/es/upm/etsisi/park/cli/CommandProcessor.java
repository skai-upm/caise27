package es.upm.etsisi.park.cli;

import es.upm.etsisi.park.core.Park;
import es.upm.etsisi.park.model.Attraction;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.service.UserOperationException;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interprets one ';'-separated command line and executes it. Commands are kept in a
 * registry, which drives both dispatching and HELP. Never throws: every failure is
 * reported on the output stream so processing can continue.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String CURRENCY = " EUR";

    private record Command(String syntax, int arity, Consumer<String[]> action) {
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Park park;
    private final PrintStream out;
    private boolean exitRequested;

    public CommandProcessor(Park park, PrintStream out) {
        this.park = park;
        this.out = out;
        registerUserCommands();
        registerSystemCommands();
        registerGroupCommands();
        registerTicketCommands();
        registerAttractionCommands();
        register("HELP", "HELP", 0, a -> printHelp());
        register("EXIT", "EXIT", 0, a -> exitRequested = true);
    }

    /**
     * Processes a single command line.
     *
     * @return false when the application must stop (EXIT), true otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        String name = normalizeName(parts[0]);
        String[] args = new String[parts.length - 1];
        for (int i = 1; i < parts.length; i++) {
            args[i - 1] = parts[i].trim();
        }
        Command command = commands.get(name);
        if (command == null) {
            error("Unknown command '" + parts[0].trim() + "'. Type HELP to list the available commands");
            return true;
        }
        if (args.length != command.arity()) {
            error("Wrong number of arguments. Usage: " + command.syntax());
            return true;
        }
        try {
            command.action().accept(args);
        } catch (UserOperationException | UncheckedIOException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Unexpected error: " + e.getMessage());
        }
        return !exitRequested;
    }

    private void registerUserCommands() {
        register("USER CREATE", "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", 7, a -> {
            park.getUserService().create(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
            out.println("USER CREATED");
        });
        register("USER DELETE", "USER DELETE;<Email>;<Password>", 2, a -> {
            park.getUserService().delete(a[0], a[1]);
            out.println("USER DELETED");
        });
        register("USER UPDATE", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>"
                + "   (Field: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD)", 4, a -> {
            park.getUserService().update(a[0], a[1], a[2], a[3]);
            out.println("USER UPDATED");
        });
    }

    private void registerSystemCommands() {
        register("SYSTEM DATE", "SYSTEM DATE;<yyyy-MM-dd>", 1,
                a -> out.println("SYSTEM DATE SET TO " + park.getSystemService().setDate(a[0])));
        register("SYSTEM PRICE", "SYSTEM PRICE;<Price>", 1,
                a -> out.println("SYSTEM PRICE SET TO " + money(park.getSystemService().setPrice(a[0]))));
    }

    private void registerGroupCommands() {
        register("GROUP CREATE", "GROUP CREATE;<Identifier>;<Name>", 2, a -> {
            park.getGroupService().create(a[0], a[1]);
            out.println("GROUP CREATED");
        });
        register("GROUP ADD", "GROUP ADD;<Identifier>;<Email>", 2, a -> {
            park.getGroupService().addMember(a[0], a[1]);
            out.println("USER ADDED TO GROUP");
        });
        register("GROUP REMOVE", "GROUP REMOVE;<Identifier>;<Email>", 2, a -> {
            park.getGroupService().removeMember(a[0], a[1]);
            out.println("USER REMOVED FROM GROUP");
        });
        register("GROUP DELETE", "GROUP DELETE;<Identifier>", 1, a -> {
            park.getGroupService().delete(a[0]);
            out.println("GROUP DELETED");
        });
    }

    private void registerTicketCommands() {
        register("TICKET CREATE", "TICKET CREATE;<identifier>;<email>;<date>", 3, a -> {
            IndividualTicket ticket = park.getTicketService().createIndividual(a[0], a[1], a[2]);
            out.println("TICKET CREATED. Price: " + money(ticket.getPrice()));
        });
        register("TICKET DELETE", "TICKET DELETE;<identifier>", 1, a -> {
            park.getTicketService().deleteIndividual(a[0]);
            out.println("TICKET DELETED");
        });
        register("GROUPTICKET CREATE", "GROUPTICKET CREATE;<identifier>;<group>;<date>", 3, a -> {
            GroupTicket ticket = park.getTicketService().createGroup(a[0], a[1], a[2]);
            int discount = ticket.getTickets().get(0).getDiscountPercent();
            out.println("GROUP TICKET CREATED. Members: " + ticket.getTickets().size() + ". Discount: " + discount
                    + "%. Total price: " + money(ticket.getPrice()));
        });
        register("GROUPTICKET DELETE", "GROUPTICKET DELETE;<identifier>", 1, a -> {
            park.getTicketService().deleteGroup(a[0]);
            out.println("GROUP TICKET DELETED");
        });
    }

    private void registerAttractionCommands() {
        register("ATTRACTION CREATE", "ATTRACTION CREATE;<identifier>;<name>;<capacity>", 3, a -> {
            park.getAttractionService().create(a[0], a[1], a[2]);
            out.println("ATTRACTION CREATED");
        });
        register("ATTRACTION ENTER", "ATTRACTION ENTER;<attraction>;<ticket>", 2, a -> {
            List<User> entered = park.getAttractionService().enter(a[0], a[1]);
            out.println("ENTERED " + a[0] + ": " + String.join(", ", emails(entered)));
        });
        register("ATTRACTION EXIT", "ATTRACTION EXIT;<attraction>;<email>", 2, a -> {
            User user = park.getAttractionService().exit(a[0], a[1]);
            out.println("EXITED " + a[0] + ": " + user.getEmail());
        });
        register("ATTRACTION LIST", "ATTRACTION LIST;<attraction>", 1, a -> {
            Attraction attraction = park.getAttractionService().find(a[0]);
            out.println("Users in " + attraction.getId() + ":");
            printOccupants(attraction);
        });
        register("ATTRACTION STATUS", "ATTRACTION STATUS;<attractionIdentifier>", 1, a -> {
            Attraction attraction = park.getAttractionService().find(a[0]);
            out.println("Attraction: " + attraction.getId());
            out.println("Name: " + attraction.getName());
            out.println("Capacity: " + attraction.getCapacity());
            out.println("Inside: " + attraction.getOccupants().size());
            out.println("Available: " + attraction.getAvailablePlaces());
            out.println("Users inside:");
            printOccupants(attraction);
        });
    }

    private void register(String name, String syntax, int arity, Consumer<String[]> action) {
        commands.put(name, new Command(syntax, arity, action));
    }

    private void printHelp() {
        out.println("Available commands:");
        for (Command command : commands.values()) {
            out.println("  " + command.syntax());
        }
    }

    private void printOccupants(Attraction attraction) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("  (none)");
        }
        for (String email : emails(attraction.getOccupants())) {
            out.println("  " + email);
        }
    }

    private static List<String> emails(List<User> users) {
        List<String> emails = new ArrayList<>();
        for (User user : users) {
            emails.add(user.getEmail());
        }
        return emails;
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString() + CURRENCY;
    }

    /** Upper-cases the command name and collapses inner whitespace ("user   create" → "USER CREATE"). */
    private static String normalizeName(String raw) {
        StringBuilder name = new StringBuilder();
        for (String word : raw.trim().split(" ")) {
            if (!word.isEmpty()) {
                if (!name.isEmpty()) {
                    name.append(' ');
                }
                name.append(word.toUpperCase(Locale.ROOT));
            }
        }
        return name.toString();
    }

    private void error(String message) {
        out.println("ERROR: " + message);
    }
}
