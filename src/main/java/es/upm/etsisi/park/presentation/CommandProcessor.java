package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.AttractionController;
import es.upm.etsisi.park.business.Attraction;
import es.upm.etsisi.park.business.GeneralAccess;
import es.upm.etsisi.park.business.GroupController;
import es.upm.etsisi.park.business.InvoiceController;
import es.upm.etsisi.park.business.ParkException;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.PassController;
import es.upm.etsisi.park.business.SpeedPass;
import es.upm.etsisi.park.business.SystemController;
import es.upm.etsisi.park.business.TicketController;
import es.upm.etsisi.park.business.User;
import es.upm.etsisi.park.business.UserController;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parses and executes one command line. Shared by interactive and file mode.
 * Commands are registered with their accepted parameter counts, syntax and
 * purpose; abnormal situations arrive as {@link ParkException}s and are reported
 * without stopping command processing.
 */
public class CommandProcessor {

    /** The controllers the commands delegate to, injected at start-up. */
    public record Controllers(UserController users, SystemController system, GroupController groups,
                              TicketController tickets, PassController passes, AttractionController attractions,
                              InvoiceController invoices) {
    }

    @FunctionalInterface
    private interface Handler {
        /** Executes the command and returns {@code false} if the application must stop. */
        boolean handle(String[] params) throws ParkException;
    }

    private record Command(Map<Integer, Handler> handlers, List<String> syntaxes, String purpose) {
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final Controllers c;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private final PrintStream out;

    public CommandProcessor(Controllers controllers, HelpGenerator help, HelpGenerator extendedHelp, PrintStream out) {
        this.c = controllers;
        this.help = help;
        this.extendedHelp = extendedHelp;
        this.out = out;
        registerUserAndSystemCommands();
        registerGroupAndTicketCommands();
        registerAttractionCommands();
        registerPassAndInvoiceCommands();
        registerControlCommands();
    }

    private void registerUserAndSystemCommands() {
        register("USER CREATE", "Registers a new user after validating DNI, email, password and card.",
                "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", 7, p -> {
                    c.users().create(p[0], p[1], p[2], p[3], p[4], p[5], p[6]);
                    return say("USER CREATED");
                });
        register("USER DELETE", "Deletes a user after checking the password, active tickets and locked groups.",
                "USER DELETE;<Email>;<Password>", 2, p -> {
                    c.users().delete(p[0], p[1]);
                    return say("USER DELETED");
                });
        register("USER UPDATE", "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of an authenticated user.",
                "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>", 4, p -> {
                    c.users().update(p[0], p[1], p[2], p[3]);
                    return say("USER UPDATED");
                });
        register("SYSTEM DATE", "Sets the current system date used by every date-dependent operation.",
                "SYSTEM DATE;<yyyy-MM-dd>", 1, p -> say("SYSTEM DATE SET TO " + c.system().setDate(p[0])));
        register("SYSTEM PRICE", "Sets the base price of tickets created from now on in this execution.",
                "SYSTEM PRICE;<Price>", 1, p -> say("SYSTEM PRICE SET TO " + c.system().setPrice(p[0]).toPlainString()));
    }

    private void registerGroupAndTicketCommands() {
        register("GROUP CREATE", "Creates an empty group of users.", "GROUP CREATE;<Identifier>;<Name>", 2, p -> {
            c.groups().create(p[0], p[1]);
            return say("GROUP CREATED");
        });
        register("GROUP ADD", "Adds a registered user to a group that has no active group ticket.",
                "GROUP ADD;<Identifier>;<Email>", 2, p -> {
                    c.groups().addMember(p[0], p[1]);
                    return say("USER ADDED TO GROUP");
                });
        register("GROUP REMOVE", "Removes a user from a group that has no active group ticket.",
                "GROUP REMOVE;<Identifier>;<Email>", 2, p -> {
                    c.groups().removeMember(p[0], p[1]);
                    return say("USER REMOVED FROM GROUP");
                });
        register("GROUP DELETE", "Deletes a group without active group tickets, keeping historical ones.",
                "GROUP DELETE;<Identifier>", 1, p -> {
                    c.groups().delete(p[0]);
                    return say("GROUP DELETED");
                });
        register("TICKET CREATE", "Buys an individual ticket for a user and a date at the current base price.",
                "TICKET CREATE;<identifier>;<email>;<date>", 3,
                p -> say("TICKET CREATED. PRICE: " + c.tickets().createIndividual(p[0], p[1], p[2]).getPrice() + " EUR"));
        register("TICKET DELETE", "Refunds a future individual ticket together with its passes.",
                "TICKET DELETE;<identifier>", 1, p -> {
                    c.tickets().deleteIndividual(p[0]);
                    return say("TICKET DELETED");
                });
        register("GROUPTICKET CREATE", "Buys a discounted ticket for every current member of a group.",
                "GROUPTICKET CREATE;<identifier>;<group>;<date>", 3,
                p -> say("GROUPTICKET CREATED. TOTAL PRICE: " + c.tickets().createGroup(p[0], p[1], p[2]).getPrice() + " EUR"));
        register("GROUPTICKET DELETE", "Refunds a future group ticket, its member tickets and their passes.",
                "GROUPTICKET DELETE;<identifier>", 1, p -> {
                    c.tickets().deleteGroup(p[0]);
                    return say("GROUPTICKET DELETED");
                });
    }

    private void registerAttractionCommands() {
        Command create = register("ATTRACTION CREATE", "Creates an attraction; without a type it is GENERAL.",
                "ATTRACTION CREATE;<identifier>;<name>;<capacity>", 3, p -> {
                    c.attractions().create(p[0], p[1], p[2], "GENERAL");
                    return say("ATTRACTION CREATED");
                });
        alternative(create, "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>", 4, p -> {
            c.attractions().create(p[0], p[1], p[2], p[3]);
            return say("ATTRACTION CREATED");
        });
        Command enter = register("ATTRACTION ENTER",
                "Lets the ticket's users in if the attraction accepts the ticket; SPEED uses one SpeedPass per user.",
                "ATTRACTION ENTER;<attraction>;<ticket>", 2, p -> say(entered(c.attractions().enter(p[0], p[1], null))));
        alternative(enter, "ATTRACTION ENTER;<attraction>;<ticket>;SPEED", 3,
                p -> say(entered(c.attractions().enter(p[0], p[1], p[2]))));
        register("ATTRACTION EXIT", "Removes a user from an attraction, releasing one place.",
                "ATTRACTION EXIT;<attraction>;<email>", 2, p -> {
                    c.attractions().exit(p[0], p[1]);
                    return say("ATTRACTION EXITED");
                });
        register("ATTRACTION LIST", "Shows the emails of the users currently inside an attraction.",
                "ATTRACTION LIST;<attraction>", 1, p -> say(list(p[0])));
        register("ATTRACTION STATUS", "Shows type, capacity, occupancy, free places and users of an attraction.",
                "ATTRACTION STATUS;<attractionIdentifier>", 1, p -> say(status(p[0])));
    }

    private void registerPassAndInvoiceCommands() {
        register("SPEEDPASS CREATE", "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket.",
                "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>", 3, p -> {
                    List<SpeedPass> passes = c.passes().buySpeedPasses(p[0], p[1], p[2]);
                    BigDecimal total = passes.stream().map(Pass::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return say("SPEEDPASS CREATED: " + ids(passes) + ". TOTAL PRICE: " + total + " EUR");
                });
        register("SPEEDPASS DELETE", "Refunds an unused SpeedPass of a future ticket.",
                "SPEEDPASS DELETE;<speedPassIdentifier>", 1, p -> {
                    c.passes().refundSpeedPass(p[0]);
                    return say("SPEEDPASS DELETED");
                });
        register("SPECIALPASS CREATE", "Buys the single Special Pass of an individual ticket, at the ticket price.",
                "SPECIALPASS CREATE;<identifier>;<individualTicket>", 2,
                p -> say("SPECIALPASS CREATED. PRICE: " + c.passes().buySpecialPass(p[0], p[1]).getPrice() + " EUR"));
        register("SPECIALPASS DELETE", "Refunds an unused Special Pass of a future ticket.",
                "SPECIALPASS DELETE;<specialPassIdentifier>", 1, p -> {
                    c.passes().refundSpecialPass(p[0]);
                    return say("SPECIALPASS DELETED");
                });
        Command invoice = register("INVOICE CREATE",
                "Shows an invoice with the tickets and passes of a user, or the group tickets of a group.",
                "INVOICE CREATE;USER;<email>", 2, p -> say(c.invoices().create(p[0], p[1]).toText()));
        invoice.syntaxes().add("INVOICE CREATE;GROUP;<groupIdentifier>");
    }

    private void registerControlCommands() {
        register("HELP", "Lists the syntax of every command.", "HELP", 0, p -> say(help.generate(commandInfo())));
        register("HELP EXTENDED", "Lists every command with an explanation of its purpose.", "HELP EXTENDED", 0,
                p -> say(extendedHelp.generate(commandInfo())));
        register("EXIT", "Ends the execution; later commands are not processed.", "EXIT", 0, p -> {
            say("BYE");
            return false;
        });
    }

    private Command register(String name, String purpose, String syntax, int parameters, Handler handler) {
        Map<Integer, Handler> handlers = new LinkedHashMap<>();
        handlers.put(parameters, handler);
        List<String> syntaxes = new ArrayList<>();
        syntaxes.add(syntax);
        Command command = new Command(handlers, syntaxes, purpose);
        commands.put(name, command);
        return command;
    }

    private static void alternative(Command command, String syntax, int parameters, Handler handler) {
        command.handlers().put(parameters, handler);
        command.syntaxes().add(syntax);
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
        Handler handler = command.handlers().get(parts.length - 1);
        if (handler == null) {
            out.println("ERROR: wrong number of parameters. Usage: " + String.join(" | ", command.syntaxes()));
            return true;
        }
        String[] params = new String[parts.length - 1];
        System.arraycopy(parts, 1, params, 0, params.length);
        try {
            return handler.handle(params);
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
            return true;
        }
    }

    /** Every registered command form, in registration order. */
    public List<CommandInfo> commandInfo() {
        List<CommandInfo> info = new ArrayList<>();
        for (Command command : commands.values()) {
            for (String syntax : command.syntaxes()) {
                info.add(new CommandInfo(syntax, command.purpose()));
            }
        }
        return info;
    }

    private static String entered(AttractionController.Entry entry) {
        StringBuilder text = new StringBuilder("ATTRACTION ENTERED: " + entry.users() + " USER(S)");
        List<Pass> consumed = entry.consumedPasses();
        if (!consumed.isEmpty()) {
            String label = consumed.get(0) instanceof SpeedPass ? "SPEEDPASS" : "SPECIAL PASS";
            text.append(". ").append(label).append(" CONSUMED: ").append(ids(consumed));
        }
        return text.toString();
    }

    private static String ids(List<? extends Pass> passes) {
        return passes.stream().map(Pass::getId).collect(Collectors.joining(", "));
    }

    private String list(String attractionId) throws ParkException {
        List<User> occupants = c.attractions().requireAttraction(attractionId).getOccupants();
        if (occupants.isEmpty()) {
            return "NO USERS INSIDE";
        }
        return occupants.stream().map(User::getEmail).collect(Collectors.joining(System.lineSeparator()));
    }

    private String status(String attractionId) throws ParkException {
        Attraction<? extends GeneralAccess> a = c.attractions().requireAttraction(attractionId);
        List<User> occupants = a.getOccupants();
        String inside = occupants.isEmpty() ? "-"
                : occupants.stream().map(User::getEmail).collect(Collectors.joining(", "));
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + a.getId(),
                "NAME: " + a.getName(),
                "TYPE: " + a.getType(),
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
