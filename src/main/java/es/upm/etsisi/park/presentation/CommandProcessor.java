package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.Attraction;
import es.upm.etsisi.park.business.EntryResult;
import es.upm.etsisi.park.business.GroupTicket;
import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.Money;
import es.upm.etsisi.park.business.ParkException;
import es.upm.etsisi.park.business.ParkServices;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.SpecialPass;
import es.upm.etsisi.park.business.SpeedPass;
import es.upm.etsisi.park.business.User;
import es.upm.etsisi.park.business.ValueParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses text commands, dispatches them through a command registry to the business controllers and
 * reports business exceptions as {@code ERROR:} lines. Interactive and file input share {@link #run}.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String SPEED = "SPEED";
    private static final String ERROR = "ERROR: ";

    private final ParkServices services;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private final Map<String, CommandDefinition> registry = new LinkedHashMap<>();
    private boolean exitRequested;

    public CommandProcessor(ParkServices services, HelpGenerator help, HelpGenerator extendedHelp) {
        this.services = services;
        this.help = help;
        this.extendedHelp = extendedHelp;
        registerUserCommands();
        registerSystemCommands();
        registerGroupAndTicketCommands();
        registerAttractionCommands();
        registerPassAndInvoiceCommands();
        registerControlCommands();
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public Collection<CommandDefinition> getCommands() {
        return registry.values();
    }

    /** Processes commands line by line until EXIT or end of input. Blank lines are ignored. */
    public void run(BufferedReader input, PrintStream output) throws IOException {
        String line;
        while (!exitRequested && (line = input.readLine()) != null) {
            if (!line.isBlank()) {
                output.println(execute(line));
            }
        }
    }

    /** Processes the commands read from a stream (for example a command file opened by the caller). */
    public void run(java.io.InputStream input, PrintStream output) {
        try {
            run(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)), output);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Executes one command and returns the text to show. Never throws for bad input. */
    public String execute(String line) {
        String[] parts = line.trim().split(SEPARATOR, -1);
        CommandDefinition command = registry.get(parts[0]);
        if (command == null) {
            return ERROR + "Unknown command: " + parts[0];
        }
        if (!command.parameterCounts().contains(parts.length - 1)) {
            return ERROR + "Malformed command: expected " + command.parameterCounts()
                    + " parameter(s) but got " + (parts.length - 1);
        }
        try {
            return command.handler().handle(parts);
        } catch (ParkException e) {
            return ERROR + e.getMessage();
        } catch (RuntimeException e) {
            return ERROR + "Unexpected failure: " + e.getMessage();
        }
    }

    private void register(String keyword, String description, CommandDefinition.Handler handler, String... syntax) {
        registry.put(keyword, new CommandDefinition(keyword, List.of(syntax), description, handler));
    }

    private void registerUserCommands() {
        register("USER CREATE", "Registers a new user after validating DNI, email, password and card; DNI and email must be unique.",
                p -> {
                    services.users().create(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    return "USER CREATED";
                }, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
        register("USER DELETE", "Deletes a user after checking the password; rejected while the user has active tickets or is in a locked group.",
                p -> {
                    services.users().delete(p[1], p[2]);
                    return "USER DELETED";
                }, "USER DELETE;<Email>;<Password>");
        register("USER UPDATE", "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of an authenticated user; the DNI never changes.",
                p -> {
                    services.users().update(p[1], p[2], p[3], p[4]);
                    return "USER UPDATED";
                }, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>");
    }

    private void registerSystemCommands() {
        register("SYSTEM DATE", "Sets the current system date used by every date-dependent operation.",
                p -> {
                    services.park().setToday(p[1]);
                    return "SYSTEM DATE SET TO " + ValueParser.formatDate(services.park().getToday());
                }, "SYSTEM DATE;<yyyy-MM-dd>");
        register("SYSTEM PRICE", "Sets the base price for tickets created from now on (40.00 at start-up).",
                p -> {
                    services.park().setBasePrice(p[1]);
                    return "SYSTEM PRICE SET TO " + Money.format(services.park().getBasePrice());
                }, "SYSTEM PRICE;<Price>");
    }

    private void registerGroupAndTicketCommands() {
        register("GROUP CREATE", "Creates an empty group with a unique identifier.",
                p -> {
                    services.groups().create(p[1], p[2]);
                    return "GROUP CREATED";
                }, "GROUP CREATE;<Identifier>;<Name>");
        register("GROUP ADD", "Adds a registered user to a group that has no active group ticket.",
                p -> {
                    services.groups().add(p[1], p[2]);
                    return "GROUP USER ADDED";
                }, "GROUP ADD;<Identifier>;<Email>");
        register("GROUP REMOVE", "Removes a member from a group that has no active group ticket.",
                p -> {
                    services.groups().remove(p[1], p[2]);
                    return "GROUP USER REMOVED";
                }, "GROUP REMOVE;<Identifier>;<Email>");
        register("GROUP DELETE", "Deletes a group that has no active group ticket; historical group tickets are kept.",
                p -> {
                    services.groups().delete(p[1]);
                    return "GROUP DELETED";
                }, "GROUP DELETE;<Identifier>");
        register("TICKET CREATE", "Buys an individual ticket for a user, for today or a later date, at the current price.",
                p -> {
                    IndividualTicket t = services.tickets().create(p[1], p[2], p[3]);
                    return "TICKET CREATED " + t.getId() + " PRICE " + Money.format(t.getPrice());
                }, "TICKET CREATE;<identifier>;<email>;<date>");
        register("TICKET DELETE", "Refunds a standalone individual ticket dated after today, together with its passes.",
                p -> {
                    services.tickets().delete(p[1]);
                    return "TICKET DELETED";
                }, "TICKET DELETE;<identifier>");
        register("GROUPTICKET CREATE", "Buys a group ticket with one discounted individual ticket per current member.",
                p -> {
                    GroupTicket t = services.tickets().createGroupTicket(p[1], p[2], p[3]);
                    return "GROUPTICKET CREATED " + t.getId() + " MEMBERS " + t.getComposition().size()
                            + " DISCOUNT " + t.getComposition().get(0).getDiscountPercent() + "% TOTAL " + Money.format(t.getPrice());
                }, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
        register("GROUPTICKET DELETE", "Refunds a group ticket dated after today with its constituent tickets and their passes.",
                p -> {
                    services.tickets().deleteGroupTicket(p[1]);
                    return "GROUPTICKET DELETED";
                }, "GROUPTICKET DELETE;<identifier>");
    }

    private void registerAttractionCommands() {
        register("ATTRACTION CREATE", "Creates an attraction; the type (GENERAL, INDIVIDUAL, GROUP, SPECIAL) defaults to GENERAL.",
                p -> {
                    services.attractions().create(p[1], p[2], p[3], p.length > 4 ? p[4] : null);
                    return "ATTRACTION CREATED";
                }, "ATTRACTION CREATE;<identifier>;<name>;<capacity>", "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>");
        register("ATTRACTION ENTER", "Admits the holders of a ticket valid today if the attraction type and capacity allow it; "
                        + "SPEED consumes one SpeedPass per person and SPECIAL attractions consume the Special Pass.",
                this::enter, "ATTRACTION ENTER;<attraction>;<ticket>", "ATTRACTION ENTER;<attraction>;<ticket>;SPEED");
        register("ATTRACTION EXIT", "Removes a user from an attraction and releases the place.",
                p -> {
                    services.attractions().exit(p[1], p[2]);
                    return "ATTRACTION EXITED";
                }, "ATTRACTION EXIT;<attraction>;<email>");
        register("ATTRACTION LIST", "Lists the emails of the users currently inside an attraction.",
                p -> {
                    List<String> inside = emails(services.attractions().require(p[1]).getOccupants());
                    return inside.isEmpty() ? "(empty)" : String.join(System.lineSeparator(), inside);
                }, "ATTRACTION LIST;<attraction>");
        register("ATTRACTION STATUS", "Shows type, capacity, occupancy, free places and users inside an attraction.",
                p -> status(services.attractions().require(p[1])), "ATTRACTION STATUS;<attractionIdentifier>");
    }

    private void registerPassAndInvoiceCommands() {
        register("SPEEDPASS CREATE", "Buys 1 or 10 SpeedPasses for an individual ticket at 20% of its price each.",
                p -> {
                    List<SpeedPass> created = services.passes().createSpeedPasses(p[1], p[2], p[3]);
                    String ids = created.size() == 1 ? created.get(0).getId()
                            : created.get(0).getId() + ".." + created.get(created.size() - 1).getId();
                    return "SPEEDPASS CREATED " + ids + " TOTAL " + Money.format(total(created));
                }, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
        register("SPEEDPASS DELETE", "Refunds an unused SpeedPass whose ticket is dated after today.",
                p -> {
                    services.passes().deleteSpeedPass(p[1]);
                    return "SPEEDPASS DELETED";
                }, "SPEEDPASS DELETE;<speedPassIdentifier>");
        register("SPECIALPASS CREATE", "Buys the single Special Pass of an individual ticket at the ticket's price.",
                p -> {
                    SpecialPass pass = services.passes().createSpecialPass(p[1], p[2]);
                    return "SPECIALPASS CREATED " + pass.getId() + " PRICE " + Money.format(pass.getPrice());
                }, "SPECIALPASS CREATE;<identifier>;<individualTicket>");
        register("SPECIALPASS DELETE", "Refunds an unused Special Pass whose ticket is dated after today.",
                p -> {
                    services.passes().deleteSpecialPass(p[1]);
                    return "SPECIALPASS DELETED";
                }, "SPECIALPASS DELETE;<specialPassIdentifier>");
        register("INVOICE CREATE", "Prints the invoice of a user (USER;<email>) or of a group (GROUP;<groupIdentifier>).",
                this::invoice, "INVOICE CREATE;USER;<email>", "INVOICE CREATE;GROUP;<groupIdentifier>");
    }

    private void registerControlCommands() {
        register("HELP", "Shows the syntax of every command.", p -> help.generate(getCommands()), "HELP");
        register("HELP EXTENDED", "Shows the syntax of every command with a short explanation.",
                p -> extendedHelp.generate(getCommands()), "HELP EXTENDED");
        register("EXIT", "Ends the application; later commands are not processed.",
                p -> {
                    exitRequested = true;
                    return "BYE";
                }, "EXIT");
    }

    private String enter(String[] p) {
        boolean speed = p.length == 4;
        if (speed && !SPEED.equals(p[3])) {
            return ERROR + "Unknown entry option: " + p[3];
        }
        EntryResult result = services.attractions().enter(p[1], p[2], speed);
        StringBuilder out = new StringBuilder("ATTRACTION ENTERED " + result.entered() + " USER(S)");
        if (!result.speedPasses().isEmpty()) {
            out.append("; SPEEDPASS CONSUMED: ").append(String.join(", ", result.speedPasses().stream().map(Pass::getId).toList()));
        }
        if (result.specialPass() != null) {
            out.append("; SPECIALPASS CONSUMED: ").append(result.specialPass().getId());
        }
        return out.toString();
    }

    private String invoice(String[] p) {
        return switch (p[1]) {
            case "USER" -> services.invoices().userInvoice(p[2]).render();
            case "GROUP" -> services.invoices().groupInvoice(p[2]).render();
            default -> ERROR + "Invoice recipient must be USER or GROUP: " + p[1];
        };
    }

    private static String status(Attraction<?> a) {
        List<String> inside = emails(a.getOccupants());
        return String.join(System.lineSeparator(),
                "ATTRACTION " + a.getId() + " - " + a.getName(),
                "Type: " + a.getType(),
                "Capacity: " + a.getCapacity(),
                "Inside: " + inside.size(),
                "Available: " + a.getAvailable(),
                "Users: " + (inside.isEmpty() ? "(none)" : String.join(", ", inside)));
    }

    private static List<String> emails(List<User> users) {
        return users.stream().map(User::getEmail).toList();
    }

    private static java.math.BigDecimal total(List<SpeedPass> passes) {
        java.math.BigDecimal sum = java.math.BigDecimal.ZERO;
        for (Pass p : passes) {
            sum = sum.add(p.getPrice());
        }
        return Money.of(sum);
    }
}
