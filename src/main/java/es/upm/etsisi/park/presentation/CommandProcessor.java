package es.upm.etsisi.park.presentation;

import es.upm.etsisi.park.business.controller.AttractionController;
import es.upm.etsisi.park.business.controller.Park;
import es.upm.etsisi.park.business.exception.ParkException;
import es.upm.etsisi.park.business.model.GroupTicket;
import es.upm.etsisi.park.business.model.IndividualTicket;
import es.upm.etsisi.park.business.model.User;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.pass.Pass;
import es.upm.etsisi.park.business.model.pass.SpecialPass;
import es.upm.etsisi.park.business.model.pass.SpeedPass;
import es.upm.etsisi.park.presentation.help.CommandInfo;
import es.upm.etsisi.park.presentation.help.HelpOption;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Interprets one ';'-separated command line and executes it through the park controllers.
 * Commands are kept in a registry, which drives dispatching and help. Business failures
 * arrive as {@link ParkException}s and are reported without stopping processing.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private record Command(CommandInfo info, int arity, Consumer<String[]> action) {
    }

    private final Map<String, List<Command>> commands = new LinkedHashMap<>();
    private final Park park;
    private final PrintStream out;
    private final InvoicePrinter invoicePrinter;
    private boolean exitRequested;

    public CommandProcessor(Park park, List<HelpOption> helpOptions, PrintStream out) {
        this.park = park;
        this.out = out;
        this.invoicePrinter = new InvoicePrinter(out);
        registerUserCommands();
        registerSystemCommands();
        registerGroupCommands();
        registerTicketCommands();
        registerAttractionCommands();
        registerPassCommands();
        registerInvoiceCommands();
        for (HelpOption option : helpOptions) {
            register(option.commandName(), option.description(), 0,
                    a -> option.generator().generate(helpCatalog()).forEach(out::println));
        }
        register("EXIT", "Ends the application; later commands are not executed.", 0, a -> exitRequested = true);
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
        List<Command> variants = commands.get(name);
        if (variants == null) {
            error("Unknown command '" + parts[0].trim() + "'. Type HELP to list the available commands");
            return true;
        }
        Command command = null;
        for (Command variant : variants) {
            if (variant.arity() == args.length) {
                command = variant;
            }
        }
        if (command == null) {
            error("Wrong number of arguments. Usage: " + String.join(" | ", syntaxesOf(variants)));
            return true;
        }
        try {
            command.action().accept(args);
        } catch (ParkException e) {
            error(e.getMessage());
        } catch (RuntimeException e) {
            error("Unexpected error: " + e.getMessage());
        }
        return !exitRequested;
    }

    private void registerUserCommands() {
        register("USER CREATE", "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
                "Registers a new user after validating DNI, email, password and card.", 7, a -> {
                    park.getUserController().create(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
                    out.println("USER CREATED");
                });
        register("USER DELETE", "USER DELETE;<Email>;<Password>",
                "Deletes a user without active tickets or locked groups.", 2, a -> {
                    park.getUserController().delete(a[0], a[1]);
                    out.println("USER DELETED");
                });
        register("USER UPDATE", "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
                "Changes NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD of a user.", 4, a -> {
                    park.getUserController().update(a[0], a[1], a[2], a[3]);
                    out.println("USER UPDATED");
                });
    }

    private void registerSystemCommands() {
        register("SYSTEM DATE", "SYSTEM DATE;<yyyy-MM-dd>", "Sets the current system date.", 1,
                a -> out.println("SYSTEM DATE SET TO " + park.getSystemController().setDate(a[0])));
        register("SYSTEM PRICE", "SYSTEM PRICE;<Price>", "Sets the base price for new tickets.", 1,
                a -> out.println("SYSTEM PRICE SET TO " + money(park.getSystemController().setPrice(a[0]))));
    }

    private void registerGroupCommands() {
        register("GROUP CREATE", "GROUP CREATE;<Identifier>;<Name>", "Creates an empty group.", 2, a -> {
            park.getGroupController().create(a[0], a[1]);
            out.println("GROUP CREATED");
        });
        register("GROUP ADD", "GROUP ADD;<Identifier>;<Email>", "Adds a user to an unlocked group.", 2, a -> {
            park.getGroupController().addMember(a[0], a[1]);
            out.println("USER ADDED TO GROUP");
        });
        register("GROUP REMOVE", "GROUP REMOVE;<Identifier>;<Email>", "Removes a user from an unlocked group.", 2, a -> {
            park.getGroupController().removeMember(a[0], a[1]);
            out.println("USER REMOVED FROM GROUP");
        });
        register("GROUP DELETE", "GROUP DELETE;<Identifier>", "Deletes a group without active group tickets.", 1, a -> {
            park.getGroupController().delete(a[0]);
            out.println("GROUP DELETED");
        });
    }

    private void registerTicketCommands() {
        register("TICKET CREATE", "TICKET CREATE;<identifier>;<email>;<date>",
                "Buys an individual ticket for a user at the current base price.", 3, a -> {
                    IndividualTicket ticket = park.getTicketController().createIndividual(a[0], a[1], a[2]);
                    out.println("TICKET CREATED. Price: " + money(ticket.getPrice()));
                });
        register("TICKET DELETE", "TICKET DELETE;<identifier>",
                "Refunds a future individual ticket and its passes.", 1, a -> {
                    park.getTicketController().deleteIndividual(a[0]);
                    out.println("TICKET DELETED");
                });
        register("GROUPTICKET CREATE", "GROUPTICKET CREATE;<identifier>;<group>;<date>",
                "Buys a discounted ticket for every member of a group.", 3, a -> {
                    GroupTicket ticket = park.getTicketController().createGroup(a[0], a[1], a[2]);
                    int discount = ticket.getTickets().get(0).getDiscountPercent();
                    out.println("GROUP TICKET CREATED. Members: " + ticket.getTickets().size() + ". Discount: "
                            + discount + "%. Total price: " + money(ticket.getPrice()));
                });
        register("GROUPTICKET DELETE", "GROUPTICKET DELETE;<identifier>",
                "Refunds a future group ticket, its member tickets and their passes.", 1, a -> {
                    park.getTicketController().deleteGroup(a[0]);
                    out.println("GROUP TICKET DELETED");
                });
    }

    private void registerAttractionCommands() {
        String createDescription = "Creates an attraction (type GENERAL, INDIVIDUAL, GROUP or SPECIAL; default GENERAL).";
        register("ATTRACTION CREATE", "ATTRACTION CREATE;<identifier>;<name>;<capacity>", createDescription, 3, a -> {
            park.getAttractionController().create(a[0], a[1], a[2], null);
            out.println("ATTRACTION CREATED");
        });
        register("ATTRACTION CREATE", "ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>", createDescription, 4,
                a -> {
                    park.getAttractionController().create(a[0], a[1], a[2], a[3]);
                    out.println("ATTRACTION CREATED");
                });
        String enterDescription = "Enters an attraction with a ticket for today; SPEED uses one SpeedPass per person.";
        register("ATTRACTION ENTER", "ATTRACTION ENTER;<attraction>;<ticket>", enterDescription, 2,
                a -> printEntry(a[0], park.getAttractionController().enter(a[0], a[1], null)));
        register("ATTRACTION ENTER", "ATTRACTION ENTER;<attraction>;<ticket>;SPEED", enterDescription, 3,
                a -> printEntry(a[0], park.getAttractionController().enter(a[0], a[1], a[2])));
        register("ATTRACTION EXIT", "ATTRACTION EXIT;<attraction>;<email>", "Leaves an attraction.", 2, a -> {
            User user = park.getAttractionController().exit(a[0], a[1]);
            out.println("EXITED " + a[0] + ": " + user.getEmail());
        });
        register("ATTRACTION LIST", "ATTRACTION LIST;<attraction>", "Lists the users inside an attraction.", 1, a -> {
            Attraction<?> attraction = park.getAttractionController().find(a[0]);
            out.println("Users in " + attraction.getId() + ":");
            printOccupants(attraction);
        });
        register("ATTRACTION STATUS", "ATTRACTION STATUS;<attractionIdentifier>",
                "Shows type, capacity, occupancy and users of an attraction.", 1, a -> {
                    Attraction<?> attraction = park.getAttractionController().find(a[0]);
                    out.println("Attraction: " + attraction.getId());
                    out.println("Name: " + attraction.getName());
                    out.println("Type: " + attraction.getType());
                    out.println("Capacity: " + attraction.getCapacity());
                    out.println("Inside: " + attraction.getOccupants().size());
                    out.println("Available: " + attraction.getAvailablePlaces());
                    out.println("Users inside:");
                    printOccupants(attraction);
                });
    }

    private void registerPassCommands() {
        register("SPEEDPASS CREATE", "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "Buys 1 or 10 SpeedPasses (20% of the ticket price each) for an individual ticket.", 3, a -> {
                    List<SpeedPass> passes = park.getPassController().buySpeedPasses(a[0], a[1], a[2]);
                    BigDecimal total = BigDecimal.ZERO;
                    for (SpeedPass pass : passes) {
                        total = total.add(pass.getPrice());
                    }
                    out.println("SPEEDPASSES CREATED: " + passes.get(0).getId() + " .. " + passes.get(passes.size() - 1).getId()
                            + ". Total price: " + money(total));
                });
        register("SPEEDPASS DELETE", "SPEEDPASS DELETE;<speedPassIdentifier>",
                "Refunds an unconsumed SpeedPass of a future ticket.", 1, a -> {
                    park.getPassController().refundSpeedPass(a[0]);
                    out.println("SPEEDPASS DELETED");
                });
        register("SPECIALPASS CREATE", "SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "Buys the Special Pass (same price as the ticket) needed for special attractions.", 2, a -> {
                    SpecialPass pass = park.getPassController().buySpecialPass(a[0], a[1]);
                    out.println("SPECIAL PASS CREATED. Price: " + money(pass.getPrice()));
                });
        register("SPECIALPASS DELETE", "SPECIALPASS DELETE;<specialPassIdentifier>",
                "Refunds an unconsumed Special Pass of a future ticket.", 1, a -> {
                    park.getPassController().refundSpecialPass(a[0]);
                    out.println("SPECIAL PASS DELETED");
                });
    }

    private void registerInvoiceCommands() {
        register("INVOICE CREATE", List.of("INVOICE CREATE;USER;<email>", "INVOICE CREATE;GROUP;<groupIdentifier>"),
                "Prints the invoice of a user's personal purchases or of a group's group tickets.", 2,
                a -> invoicePrinter.print(park.getInvoiceController().create(a[0], a[1])));
    }

    private void register(String name, String syntax, String description, int arity, Consumer<String[]> action) {
        register(name, List.of(syntax), description, arity, action);
    }

    private void register(String name, String description, int arity, Consumer<String[]> action) {
        register(name, List.of(name), description, arity, action);
    }

    private void register(String name, List<String> syntaxes, String description, int arity, Consumer<String[]> action) {
        commands.computeIfAbsent(name, key -> new ArrayList<>())
                .add(new Command(new CommandInfo(syntaxes, description), arity, action));
    }

    private List<CommandInfo> helpCatalog() {
        List<CommandInfo> catalog = new ArrayList<>();
        for (List<Command> variants : commands.values()) {
            for (Command command : variants) {
                catalog.add(command.info());
            }
        }
        return catalog;
    }

    private static List<String> syntaxesOf(List<Command> variants) {
        List<String> syntaxes = new ArrayList<>();
        for (Command command : variants) {
            syntaxes.addAll(command.info().syntaxes());
        }
        return syntaxes;
    }

    private void printEntry(String attractionId, AttractionController.Entry entry) {
        List<String> emails = new ArrayList<>();
        for (User user : entry.users()) {
            emails.add(user.getEmail());
        }
        out.println("ENTERED " + attractionId + ": " + String.join(", ", emails));
        for (Pass pass : entry.consumedPasses()) {
            out.println(pass.getKind() + " consumed: " + pass.getId());
        }
    }

    private void printOccupants(Attraction<?> attraction) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("  (none)");
        }
        for (User user : attraction.getOccupants()) {
            out.println("  " + user.getEmail());
        }
    }

    private static String money(BigDecimal amount) {
        return InvoicePrinter.money(amount);
    }

    /** Upper-cases the command name and collapses inner whitespace ("help   extended" → "HELP EXTENDED"). */
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
