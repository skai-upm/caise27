package es.upm.park.command;

import es.upm.park.controller.ParkController;
import es.upm.park.controller.ParkException;

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

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>",
            "USER DELETE;<email>;<password>",
            "USER UPDATE;<email>;<password>;<field>;<value>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
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

    /** Parses a yyyy-MM-dd date argument, rejecting impossible calendar dates. */
    static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            throw new ParkException("Invalid date, expected yyyy-MM-dd: " + text.trim());
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
