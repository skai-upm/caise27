package es.upm.amusementpark;

import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Interprets one ';'-separated command line and executes it. Never throws for bad input: every
 * problem is reported as an "ERROR: ..." message.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>   (Field: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "HELP",
            "EXIT");

    /**
     * Outcome of a command: the text to show and whether the application must stop.
     */
    public record Result(String output, boolean exit) {
    }

    private final UserService userService = new UserService();
    private final SystemService systemService = new SystemService();
    private final GroupService groupService = new GroupService();
    private final TicketService ticketService = new TicketService();
    private final AttractionService attractionService = new AttractionService();

    public Result process(String line) {
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        String command = parts[0];
        try {
            switch (command) {
                case "USER CREATE":
                    requireArguments(parts, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
                    userService.create(parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7]);
                    return ok("USER CREATED");
                case "USER DELETE":
                    requireArguments(parts, "USER DELETE;<Email>;<Password>");
                    userService.delete(parts[1], parts[2]);
                    return ok("USER DELETED");
                case "USER UPDATE":
                    requireArguments(parts, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>");
                    UserField field = UserField.fromName(parts[3]);
                    if (field == null) {
                        throw new IllegalArgumentException("field " + parts[3]
                                + " cannot be modified (allowed: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD)");
                    }
                    userService.update(parts[1], parts[2], field, parts[4]);
                    return ok("USER UPDATED");
                case "SYSTEM DATE":
                    requireArguments(parts, "SYSTEM DATE;<yyyy-MM-dd>");
                    return ok("SYSTEM DATE SET TO " + ParkFormat.formatDate(systemService.setDate(parts[1])));
                case "SYSTEM PRICE":
                    requireArguments(parts, "SYSTEM PRICE;<Price>");
                    return ok("SYSTEM PRICE SET TO " + ParkFormat.formatPrice(systemService.setPrice(parts[1])));
                case "GROUP CREATE":
                    requireArguments(parts, "GROUP CREATE;<Identifier>;<Name>");
                    groupService.create(parts[1], parts[2]);
                    return ok("GROUP CREATED");
                case "GROUP ADD":
                    requireArguments(parts, "GROUP ADD;<Identifier>;<Email>");
                    groupService.add(parts[1], parts[2]);
                    return ok("USER ADDED TO GROUP");
                case "GROUP REMOVE":
                    requireArguments(parts, "GROUP REMOVE;<Identifier>;<Email>");
                    groupService.remove(parts[1], parts[2]);
                    return ok("USER REMOVED FROM GROUP");
                case "GROUP DELETE":
                    requireArguments(parts, "GROUP DELETE;<Identifier>");
                    groupService.delete(parts[1]);
                    return ok("GROUP DELETED");
                case "TICKET CREATE":
                    requireArguments(parts, "TICKET CREATE;<identifier>;<email>;<date>");
                    return ok("TICKET CREATED. PRICE: "
                            + euros(ticketService.createIndividual(parts[1], parts[2], parts[3]).getPrice()));
                case "TICKET DELETE":
                    requireArguments(parts, "TICKET DELETE;<identifier>");
                    ticketService.deleteIndividual(parts[1]);
                    return ok("TICKET DELETED");
                case "GROUPTICKET CREATE":
                    requireArguments(parts, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
                    GroupTicket groupTicket = ticketService.createGroup(parts[1], parts[2], parts[3]);
                    return ok("GROUP TICKET CREATED FOR " + groupTicket.getComposition().size()
                            + " USER(S). TOTAL PRICE: " + euros(groupTicket.getPrice()));
                case "GROUPTICKET DELETE":
                    requireArguments(parts, "GROUPTICKET DELETE;<identifier>");
                    ticketService.deleteGroup(parts[1]);
                    return ok("GROUP TICKET DELETED");
                case "ATTRACTION CREATE":
                    requireArguments(parts, "ATTRACTION CREATE;<identifier>;<name>;<capacity>");
                    attractionService.create(parts[1], parts[2], parts[3]);
                    return ok("ATTRACTION CREATED");
                case "ATTRACTION ENTER":
                    requireArguments(parts, "ATTRACTION ENTER;<attraction>;<ticket>");
                    List<User> entered = attractionService.enter(parts[1], parts[2]);
                    return ok("ENTERED ATTRACTION " + parts[1] + ": " + emails(entered));
                case "ATTRACTION EXIT":
                    requireArguments(parts, "ATTRACTION EXIT;<attraction>;<email>");
                    attractionService.exit(parts[1], parts[2]);
                    return ok("EXITED ATTRACTION " + parts[1] + ": " + parts[2]);
                case "ATTRACTION LIST":
                    requireArguments(parts, "ATTRACTION LIST;<attraction>");
                    return ok(list(attractionService.existing(parts[1])));
                case "ATTRACTION STATUS":
                    requireArguments(parts, "ATTRACTION STATUS;<attractionIdentifier>");
                    return ok(status(attractionService.existing(parts[1])));
                case "HELP":
                    requireArguments(parts, "HELP");
                    return ok(HELP_TEXT);
                case "EXIT":
                    requireArguments(parts, "EXIT");
                    return new Result("BYE", true);
                default:
                    return error("unknown command '" + command + "'. Type HELP to list the available commands");
            }
        } catch (IllegalArgumentException | UncheckedIOException e) {
            return error(e.getMessage());
        }
    }

    /**
     * Checks that the line has as many values as the syntax declares and that none is blank.
     */
    private static void requireArguments(String[] parts, String syntax) {
        int expected = syntax.split(";", -1).length - 1;
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("wrong number of arguments. Usage: " + syntax);
        }
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                throw new IllegalArgumentException("empty value in argument " + i + ". Usage: " + syntax);
            }
        }
    }

    private static String list(Attraction attraction) {
        if (attraction.getOccupants().isEmpty()) {
            return "NO USERS IN ATTRACTION " + attraction.getId();
        }
        List<String> lines = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            lines.add(user.getEmail());
        }
        return String.join(System.lineSeparator(), lines);
    }

    private static String status(Attraction attraction) {
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + attraction.getId(),
                "NAME: " + attraction.getName(),
                "CAPACITY: " + attraction.getCapacity(),
                "USERS INSIDE: " + attraction.getOccupants().size(),
                "AVAILABLE PLACES: " + attraction.getAvailablePlaces(),
                "OCCUPANTS: " + (attraction.getOccupants().isEmpty() ? "-" : emails(attraction.getOccupants())));
    }

    private static String emails(List<User> users) {
        List<String> emails = new ArrayList<>();
        for (User user : users) {
            emails.add(user.getEmail());
        }
        return String.join(", ", emails);
    }

    private static String euros(BigDecimal amount) {
        return ParkFormat.formatPrice(amount) + " EUR";
    }

    private static Result ok(String output) {
        return new Result(output, false);
    }

    private static Result error(String message) {
        return new Result("ERROR: " + message, false);
    }
}
