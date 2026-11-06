package es.upm.park.presentation;

import es.upm.park.business.controller.ParkController;
import es.upm.park.business.exception.InvalidInputException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.Attraction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Interprets one command line, independently of where the line comes from.
 * A line is split on ';': the first segment is the command keyword and the
 * remaining segments are its arguments.
 */
public class CommandProcessor {

    /** Command syntax shown by HELP, exactly as mandated for Delivery 2. */
    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
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
            "INVOICE CREATE;USER;<email>",
            "INVOICE CREATE;GROUP;<groupIdentifier>",
            "HELP",
            "EXIT");

    static final String EXIT_TEXT = "Exiting the application.";

    private final Map<String, Function<String[], CommandResult>> handlers = new LinkedHashMap<>();

    public CommandProcessor() {
        handlers.put("HELP", args -> CommandResult.of(HELP_TEXT));
        handlers.put("EXIT", args -> CommandResult.exit(EXIT_TEXT));
        handlers.put("SYSTEM DATE", this::systemDate);
        handlers.put("SYSTEM PRICE", this::systemPrice);
        handlers.put("GROUP CREATE", args -> withArgs(args, 2, "GROUP CREATE;<Identifier>;<Name>", a -> {
            park().createGroup(a[0], a[1]);
            return "GROUP CREATED";
        }));
        handlers.put("GROUP ADD", args -> withArgs(args, 2, "GROUP ADD;<Identifier>;<Email>", a -> {
            park().addUserToGroup(a[0], a[1]);
            return "USER ADDED TO GROUP";
        }));
        handlers.put("GROUP REMOVE", args -> withArgs(args, 2, "GROUP REMOVE;<Identifier>;<Email>", a -> {
            park().removeUserFromGroup(a[0], a[1]);
            return "USER REMOVED FROM GROUP";
        }));
        handlers.put("GROUP DELETE", args -> withArgs(args, 1, "GROUP DELETE;<Identifier>", a -> {
            park().deleteGroup(a[0]);
            return "GROUP DELETED";
        }));
        handlers.put("TICKET CREATE", args -> withArgs(args, 3, "TICKET CREATE;<identifier>;<email>;<date>", a ->
                "TICKET CREATED. PRICE: " + park().createIndividualTicket(a[0], a[1], parseDate(a[2])).getPrice()));
        handlers.put("TICKET DELETE", args -> withArgs(args, 1, "TICKET DELETE;<identifier>", a -> {
            park().deleteTicket(a[0]);
            return "TICKET DELETED";
        }));
        handlers.put("GROUPTICKET CREATE", args -> withArgs(args, 3, "GROUPTICKET CREATE;<identifier>;<group>;<date>",
                a -> "GROUP TICKET CREATED. TOTAL PRICE: "
                        + park().createGroupTicket(a[0], a[1], parseDate(a[2])).getPrice()));
        handlers.put("GROUPTICKET DELETE", args -> withArgs(args, 1, "GROUPTICKET DELETE;<identifier>", a -> {
            park().deleteGroupTicket(a[0]);
            return "GROUP TICKET DELETED";
        }));
        handlers.put("ATTRACTION CREATE", args -> withArgs(args, 3, "ATTRACTION CREATE;<identifier>;<name>;<capacity>", a -> {
            park().createAttraction(a[0], a[1], parseCapacity(a[2]));
            return "ATTRACTION CREATED";
        }));
        handlers.put("ATTRACTION LIST", args -> withArgs(args, 1, "ATTRACTION LIST;<attraction>",
                a -> listOccupants(park().requireAttraction(a[0]))));
        handlers.put("ATTRACTION ENTER", args -> withArgs(args, 2,
                "ATTRACTION ENTER;<attraction>;<ticket>",
                a -> "ENTERED " + a[0] + ": " + String.join(", ", park().enterAttraction(a[0], a[1]))));
        handlers.put("ATTRACTION EXIT", args -> withArgs(args, 2, "ATTRACTION EXIT;<attraction>;<email>", a -> {
            park().exitAttraction(a[0], a[1]);
            return "EXITED " + a[0] + ": " + a[1];
        }));
        handlers.put("INVOICE CREATE", args -> withArgs(args, 2, "INVOICE CREATE;USER;<email> | INVOICE CREATE;GROUP;<groupIdentifier>",
                a -> switch (a[0]) {
                    case "USER" -> park().userInvoice(a[1]).render();
                    case "GROUP" -> park().groupInvoice(a[1]).render();
                    default -> throw new InvalidInputException("Invoice type must be USER or GROUP: " + a[0]);
                }));
        handlers.put("ATTRACTION STATUS", args -> withArgs(args, 1, "ATTRACTION STATUS;<attractionIdentifier>",
                a -> status(park().requireAttraction(a[0]))));
    }

    public CommandResult process(String line) {
        String[] parts = (line == null ? "" : line).split(";", -1);
        String keyword = parts[0].trim();
        Function<String[], CommandResult> handler = handlers.get(keyword);
        if (handler == null) {
            return error("Unknown command: " + (line == null ? "" : line.trim()));
        }
        try {
            return handler.apply(Arrays.copyOfRange(parts, 1, parts.length));
        } catch (ParkException e) {
            return error(e.getMessage());
        }
    }

    /**
     * Checks the argument count, trims the arguments and runs the action, whose
     * return value is the confirmation message.
     */
    private static CommandResult withArgs(String[] args, int count, String usage,
                                          Function<String[], String> action) {
        if (args.length != count) {
            return error("Usage: " + usage);
        }
        String[] trimmed = new String[count];
        for (int i = 0; i < count; i++) {
            trimmed[i] = args[i].trim();
        }
        return CommandResult.of(action.apply(trimmed));
    }

    private static ParkController park() {
        return ParkController.getInstance();
    }

    private static int parseCapacity(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("The attraction capacity must be a positive number: " + text);
        }
    }

    private static String listOccupants(Attraction attraction) {
        if (attraction.getOccupantEmails().isEmpty()) {
            return "NO USERS IN " + attraction.getId();
        }
        StringBuilder out = new StringBuilder("USERS IN " + attraction.getId() + ":");
        for (String email : attraction.getOccupantEmails()) {
            out.append(System.lineSeparator()).append(email);
        }
        return out.toString();
    }

    private static String status(Attraction attraction) {
        int inside = attraction.getOccupantEmails().size();
        String users = inside == 0 ? "none" : String.join(", ", attraction.getOccupantEmails());
        return "ATTRACTION " + attraction.getId() + " - " + attraction.getName()
                + " | CAPACITY: " + attraction.getCapacity()
                + " | INSIDE: " + inside
                + " | AVAILABLE: " + (attraction.getCapacity() - inside)
                + System.lineSeparator() + "USERS: " + users;
    }

    /** Parses a yyyy-MM-dd date argument, rejecting impossible calendar dates. */
    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new InvalidInputException("Invalid date, expected yyyy-MM-dd: " + text.trim());
        }
    }

    private CommandResult systemDate(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            return error("Usage: SYSTEM DATE;<yyyy-MM-dd>");
        }
        LocalDate date = parseDate(args[0]);
        ParkController.getInstance().setCurrentDate(date);
        return CommandResult.of("SYSTEM DATE UPDATED: " + date);
    }

    private CommandResult systemPrice(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            return error("Usage: SYSTEM PRICE;<Price>");
        }
        BigDecimal price;
        try {
            price = new BigDecimal(args[0].trim());
        } catch (NumberFormatException e) {
            return error("Invalid price: " + args[0].trim());
        }
        ParkController.getInstance().setBasePrice(price);
        return CommandResult.of("SYSTEM PRICE UPDATED: " + ParkController.getInstance().getBasePrice());
    }

    private static CommandResult error(String message) {
        return CommandResult.of("ERROR: " + message);
    }
}
