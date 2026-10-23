package es.upm.park.presentation;

import es.upm.park.business.controller.AttractionController;
import es.upm.park.business.controller.GroupController;
import es.upm.park.business.controller.InvoiceController;
import es.upm.park.business.controller.Park;
import es.upm.park.business.controller.PassController;
import es.upm.park.business.controller.TicketController;
import es.upm.park.business.controller.UserController;
import es.upm.park.business.exception.ParkException;
import es.upm.park.business.exception.ValidationException;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
import es.upm.park.business.model.attraction.Attraction;
import es.upm.park.business.model.attraction.AttractionType;
import es.upm.park.business.model.attraction.EntryResult;
import es.upm.park.business.model.pass.Pass;
import es.upm.park.business.model.pass.SpecialPass;
import es.upm.park.business.model.pass.SpeedPass;
import es.upm.park.presentation.help.CommandHelp;
import es.upm.park.presentation.help.HelpGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets ';'-separated text commands using basic String operations and
 * dispatches them to the business controllers. The same instance serves
 * both interactive and file input. It is the single place where failed
 * operations become {@code ERROR:} messages.
 */
public class CommandProcessor {

    static final String SEPARATOR = ";";
    static final String EXIT_COMMAND = "EXIT";
    static final String SPEED_OPTION = "SPEED";
    private static final String CURRENCY = " EUR";

    @FunctionalInterface
    private interface Handler {
        String handle(String[] fields) throws ParkException;
    }

    /**
     * One accepted form of a command: its syntax and number of fields.
     */
    private record Variant(String syntax, String description, int fieldCount) {
    }

    private record CommandDefinition(List<Variant> variants, Handler handler) {
    }

    /**
     * Controllers the commands are dispatched to.
     */
    public record Controllers(Park park, UserController users, GroupController groups, TicketController tickets,
                              PassController passes, AttractionController attractions,
                              InvoiceController invoices) {
    }

    private final Controllers controllers;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private final InvoiceFormatter invoiceFormatter = new InvoiceFormatter();
    private final Map<String, CommandDefinition> commands = new LinkedHashMap<>();

    public CommandProcessor(Controllers controllers, HelpGenerator help, HelpGenerator extendedHelp) {
        this.controllers = controllers;
        this.help = help;
        this.extendedHelp = extendedHelp;
        registerCommands();
    }

    private void registerCommands() {
        Park park = controllers.park();
        register("USER CREATE", f -> {
            controllers.users().create(f[1], f[2], f[3], f[4], f[5], f[6], f[7]);
            return "USER CREATED";
        }, variant("USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "Registers a user after validating DNI, email, password and card.", 8));
        register("USER DELETE", f -> {
            controllers.users().delete(f[1], f[2]);
            return "USER DELETED";
        }, variant("USER DELETE;<Email>;<Password>",
                "Deletes a user without active tickets or locked groups.", 3));
        register("USER UPDATE", f -> {
            controllers.users().update(f[1], f[2], f[3], f[4]);
            return "USER UPDATED";
        }, variant("USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user.", 5));
        register("SYSTEM DATE", f -> {
            LocalDate date = ValueParser.parseDate(f[1]);
            park.setCurrentDate(date);
            return "SYSTEM DATE SET TO " + ValueParser.formatDate(date);
        }, variant("SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date used by every operation.", 2));
        register("SYSTEM PRICE", f -> {
            BigDecimal price = ValueParser.parsePrice(f[1]);
            park.setCurrentPrice(price);
            return "SYSTEM PRICE SET TO " + price.toPlainString();
        }, variant("SYSTEM PRICE;<Price>", "Sets the base price of tickets created from now on.", 2));
        register("GROUP CREATE", f -> {
            controllers.groups().create(f[1], f[2]);
            return "GROUP CREATED";
        }, variant("GROUP CREATE;<Identifier>;<Name>", "Creates an empty group.", 3));
        register("GROUP ADD", f -> {
            controllers.groups().addMember(f[1], f[2]);
            return "USER ADDED TO GROUP";
        }, variant("GROUP ADD;<Identifier>;<Email>", "Adds a user to a group without active group tickets.", 3));
        register("GROUP REMOVE", f -> {
            controllers.groups().removeMember(f[1], f[2]);
            return "USER REMOVED FROM GROUP";
        }, variant("GROUP REMOVE;<Identifier>;<Email>",
                "Removes a user from a group without active group tickets.", 3));
        register("GROUP DELETE", f -> {
            controllers.groups().delete(f[1]);
            return "GROUP DELETED";
        }, variant("GROUP DELETE;<Identifier>", "Deletes a group without active group tickets.", 2));
        register("TICKET CREATE", f -> {
            IndividualTicket ticket = controllers.tickets().createTicket(f[1], f[2], ValueParser.parseDate(f[3]));
            return "TICKET CREATED. PRICE: " + money(ticket.getPrice());
        }, variant("TICKET CREATE;<identifier>;<email>;<date>",
                "Buys an individual ticket for a user at the current price.", 4));
        register("TICKET DELETE", f -> {
            controllers.tickets().deleteTicket(f[1]);
            return "TICKET DELETED";
        }, variant("TICKET DELETE;<identifier>",
                "Refunds a future individual ticket together with its passes.", 2));
        register("GROUPTICKET CREATE", f -> {
            GroupTicket ticket = controllers.tickets().createGroupTicket(f[1], f[2], ValueParser.parseDate(f[3]));
            return "GROUP TICKET CREATED (" + ticket.getComposition().size() + " tickets). TOTAL PRICE: "
                    + money(ticket.getPrice());
        }, variant("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                "Buys a discounted ticket for every current member of a group.", 4));
        register("GROUPTICKET DELETE", f -> {
            controllers.tickets().deleteGroupTicket(f[1]);
            return "GROUP TICKET DELETED";
        }, variant("GROUPTICKET DELETE;<identifier>",
                "Refunds a future group ticket, its member tickets and their passes.", 2));
        register("ATTRACTION CREATE", this::createAttraction,
                variant("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                        "Creates a GENERAL attraction.", 4),
                variant("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                        "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL.", 5));
        register("ATTRACTION ENTER", this::enterAttraction,
                variant("ATTRACTION ENTER;<attraction>;<ticket>",
                        "Lets the ticket's users in (SPECIAL attractions use the Special Pass).", 3),
                variant("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                        "Lets the ticket's users in consuming one SpeedPass per ticket.", 4));
        register("ATTRACTION EXIT", f -> {
            controllers.attractions().exit(f[1], f[2]);
            return "ATTRACTION EXITED";
        }, variant("ATTRACTION EXIT;<attraction>;<email>", "Lets a user leave an attraction.", 3));
        register("ATTRACTION LIST", f -> {
            Attraction<?> attraction = controllers.attractions().existingAttraction(f[1]);
            List<User> occupants = attraction.getOccupants();
            if (occupants.isEmpty()) {
                return "NO USERS IN ATTRACTION " + attraction.getId();
            }
            return String.join(System.lineSeparator(), emails(occupants));
        }, variant("ATTRACTION LIST;<attraction>", "Lists the emails of the users inside an attraction.", 2));
        register("ATTRACTION STATUS", f -> formatStatus(controllers.attractions().existingAttraction(f[1])),
                variant("ATTRACTION STATUS;<attractionIdentifier>",
                        "Shows type, capacity, occupancy and users of an attraction.", 2));
        register("SPEEDPASS CREATE", f -> {
            List<SpeedPass> passes = controllers.passes().buySpeedPasses(f[1], f[2], ValueParser.parseQuantity(f[3]));
            BigDecimal total = BigDecimal.ZERO.setScale(2);
            for (SpeedPass pass : passes) {
                total = total.add(pass.getPrice());
            }
            return "SPEEDPASS CREATED: " + passes.get(0).getId()
                    + (passes.size() > 1 ? " to " + passes.get(passes.size() - 1).getId() : "")
                    + ". TOTAL PRICE: " + money(total);
        }, variant("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket.", 4));
        register("SPEEDPASS DELETE", f -> {
            controllers.passes().refundSpeedPass(f[1]);
            return "SPEEDPASS DELETED";
        }, variant("SPEEDPASS DELETE;<speedPassIdentifier>",
                "Refunds an unconsumed SpeedPass of a future ticket.", 2));
        register("SPECIALPASS CREATE", f -> {
            SpecialPass pass = controllers.passes().buySpecialPass(f[1], f[2]);
            return "SPECIALPASS CREATED: " + pass.getId() + ". PRICE: " + money(pass.getPrice());
        }, variant("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "Buys the Special Pass (same price as the ticket) needed by SPECIAL attractions.", 3));
        register("SPECIALPASS DELETE", f -> {
            controllers.passes().refundSpecialPass(f[1]);
            return "SPECIALPASS DELETED";
        }, variant("SPECIALPASS DELETE;<specialPassIdentifier>",
                "Refunds an unconsumed Special Pass of a future ticket.", 2));
        register("INVOICE CREATE", this::createInvoice,
                variant("INVOICE CREATE;USER;<email>",
                        "Shows a user's invoice: own tickets and personal passes.", 3),
                variant("INVOICE CREATE;GROUP;<groupIdentifier>",
                        "Shows a group's invoice: its group tickets and their composition.", 3));
        register("HELP", f -> help.generate(helpEntries()),
                variant("HELP", "Lists the syntax of every command.", 1));
        register("HELP EXTENDED", f -> extendedHelp.generate(helpEntries()),
                variant("HELP EXTENDED", "Lists every command with an explanation of its purpose.", 1));
        register(EXIT_COMMAND, f -> EXIT_COMMAND,
                variant(EXIT_COMMAND, "Ends the application; later commands are not executed.", 1));
    }

    private static Variant variant(String syntax, String description, int fieldCount) {
        return new Variant(syntax, description, fieldCount);
    }

    private void register(String name, Handler handler, Variant... variants) {
        commands.put(name, new CommandDefinition(List.of(variants), handler));
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
        CommandDefinition definition = commands.get(name);
        if (definition == null) {
            return CommandResult.error("Unknown command: " + fields[0] + ". Type HELP for the list of commands");
        }
        if (definition.variants().stream().noneMatch(v -> v.fieldCount() == fields.length)) {
            return CommandResult.error("Usage: " + String.join(" | ",
                    definition.variants().stream().map(Variant::syntax).toList()));
        }
        try {
            String output = definition.handler().handle(fields);
            return EXIT_COMMAND.equals(name) ? CommandResult.exit(output) : CommandResult.message(output);
        } catch (ParkException e) {
            return CommandResult.error(e.getMessage());
        } catch (RuntimeException e) {
            return CommandResult.error("Unexpected problem processing the command: " + e.getMessage());
        }
    }

    /**
     * Syntax and purpose of every accepted command form, in registration order.
     */
    public List<CommandHelp> helpEntries() {
        List<CommandHelp> entries = new ArrayList<>();
        for (CommandDefinition definition : commands.values()) {
            for (Variant variant : definition.variants()) {
                entries.add(new CommandHelp(variant.syntax(), variant.description()));
            }
        }
        return entries;
    }

    private String createAttraction(String[] f) throws ParkException {
        AttractionType type = AttractionType.GENERAL;
        if (f.length == 5) {
            type = AttractionType.parse(f[4]);
            if (type == null) {
                throw new ValidationException("Unknown attraction type " + f[4]
                        + " (expected GENERAL, INDIVIDUAL, GROUP or SPECIAL)");
            }
        }
        controllers.attractions().create(f[1], f[2], ValueParser.parseCapacity(f[3]), type);
        return "ATTRACTION CREATED";
    }

    private String enterAttraction(String[] f) throws ParkException {
        boolean speed = f.length == 4;
        if (speed && !SPEED_OPTION.equalsIgnoreCase(f[3])) {
            throw new ValidationException("Unknown entry option " + f[3] + " (only SPEED is allowed)");
        }
        EntryResult result = controllers.attractions().enter(f[1], f[2], speed);
        StringBuilder output = new StringBuilder("ATTRACTION ENTERED: " + result.entered().size() + " user(s)");
        List<String> speedPasses = new ArrayList<>();
        for (Pass pass : result.consumedPasses()) {
            if (pass instanceof SpecialPass) {
                output.append(System.lineSeparator()).append("SPECIAL PASS CONSUMED: ").append(pass.getId());
            } else {
                speedPasses.add(pass.getId());
            }
        }
        if (!speedPasses.isEmpty()) {
            output.append(System.lineSeparator()).append("SPEEDPASS CONSUMED: ").append(String.join(", ", speedPasses));
        }
        return output.toString();
    }

    private String createInvoice(String[] f) throws ParkException {
        String target = f[1].toUpperCase();
        return switch (target) {
            case "USER" -> invoiceFormatter.format(controllers.invoices().userInvoice(f[2]));
            case "GROUP" -> invoiceFormatter.format(controllers.invoices().groupInvoice(f[2]));
            default -> throw new ValidationException("Invoice target must be USER or GROUP, not " + f[1]);
        };
    }

    private static String formatStatus(Attraction<?> attraction) {
        List<User> occupants = attraction.getOccupants();
        String users = occupants.isEmpty() ? "(none)" : String.join(", ", emails(occupants));
        return String.join(System.lineSeparator(),
                "ATTRACTION " + attraction.getId(),
                "  Name: " + attraction.getName(),
                "  Type: " + attraction.getType(),
                "  Capacity: " + attraction.getCapacity(),
                "  Users inside: " + attraction.getOccupancy(),
                "  Available places: " + attraction.getAvailablePlaces(),
                "  Users: " + users);
    }

    private static List<String> emails(List<User> users) {
        return users.stream().map(User::getEmail).toList();
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString() + CURRENCY;
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
