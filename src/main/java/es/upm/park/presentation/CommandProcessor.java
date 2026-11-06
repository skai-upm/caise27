package es.upm.park.presentation;

import es.upm.park.business.controller.InvoiceController;
import es.upm.park.business.controller.ParkController;
import es.upm.park.business.exception.InvalidInputException;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.presentation.help.CommandCatalog;
import es.upm.park.presentation.help.ExtendedHelpFormatter;
import es.upm.park.presentation.help.HelpGenerator;
import es.upm.park.presentation.help.SyntaxHelpFormatter;

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

    static final String EXIT_TEXT = "Exiting the application.";

    private final Map<String, Function<String[], CommandResult>> handlers = new LinkedHashMap<>();
    private final ParkController parkController;
    private final InvoiceController invoiceController;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;

    /** Uses the execution's controller instances and the standard help formats. */
    public CommandProcessor() {
        this(ParkController.getInstance(), InvoiceController.getInstance(),
                new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter()),
                new HelpGenerator(CommandCatalog.standard(), new ExtendedHelpFormatter()));
    }

    /** Uses the controllers and help generators injected by the application configuration. */
    public CommandProcessor(ParkController parkController, InvoiceController invoiceController,
                            HelpGenerator help, HelpGenerator extendedHelp) {
        this.parkController = parkController;
        this.invoiceController = invoiceController;
        this.help = help;
        this.extendedHelp = extendedHelp;
        handlers.put("HELP", args -> CommandResult.of(help.generate()));
        handlers.put("HELP EXTENDED", args -> CommandResult.of(extendedHelp.generate()));
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
        handlers.put("SPEEDPASS CREATE", args -> withArgs(args, 3,
                "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>", a -> {
                    var bought = park().buySpeedPasses(a[0], a[1], parseQuantity(a[2]));
                    var total = bought.stream().map(SpeedPass::getPrice)
                            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                    return "SPEEDPASS PURCHASED. TOTAL PRICE: " + total;
                }));
        handlers.put("SPEEDPASS DELETE", args -> withArgs(args, 1, "SPEEDPASS DELETE;<speedPassIdentifier>", a -> {
            park().refundPass(a[0], SpeedPass.class);
            return "SPEEDPASS REFUNDED";
        }));
        handlers.put("SPECIALPASS CREATE", args -> withArgs(args, 2, "SPECIALPASS CREATE;<identifier>;<individualTicket>",
                a -> "SPECIALPASS PURCHASED. PRICE: " + park().buySpecialPass(a[0], a[1]).getPrice()));
        handlers.put("SPECIALPASS DELETE", args -> withArgs(args, 1, "SPECIALPASS DELETE;<specialPassIdentifier>", a -> {
            park().refundPass(a[0], SpecialPass.class);
            return "SPECIALPASS REFUNDED";
        }));
        handlers.put("INVOICE CREATE", args -> withArgs(args, 2, "INVOICE CREATE;USER;<email> | INVOICE CREATE;GROUP;<groupIdentifier>",
                a -> switch (a[0]) {
                    case "USER" -> invoiceController.userInvoice(a[1]).render();
                    case "GROUP" -> invoiceController.groupInvoice(a[1]).render();
                    default -> throw new InvalidInputException("Invoice type must be USER or GROUP: " + a[0]);
                }));
        handlers.put("ATTRACTION STATUS", args -> withArgs(args, 1, "ATTRACTION STATUS;<attractionIdentifier>",
                a -> status(park().requireAttraction(a[0]))));
    }

    /** Keywords of the commands this processor can execute. */
    java.util.Set<String> handledCommands() {
        return java.util.Collections.unmodifiableSet(handlers.keySet());
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

    private ParkController park() {
        return parkController;
    }

    private static int parseQuantity(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new InvalidInputException("SpeedPasses are sold in quantities of 1 or 10: " + text);
        }
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
        park().setCurrentDate(date);
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
        park().setBasePrice(price);
        return CommandResult.of("SYSTEM PRICE UPDATED: " + park().getBasePrice());
    }

    private static CommandResult error(String message) {
        return CommandResult.of("ERROR: " + message);
    }
}
