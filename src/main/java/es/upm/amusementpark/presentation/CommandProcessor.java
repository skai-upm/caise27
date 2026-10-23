package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassType;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.controller.EntryResult;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.invoice.Invoice;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Interprets one {@code ;}-separated command line and executes it against the
 * {@link Park}. Parsing relies only on basic String operations and
 * {@code split}. Every command is registered once with its syntax, which is
 * also used to check the number of arguments and to build the HELP text.
 */
public class CommandProcessor {

    static final String USER_CREATE_SYNTAX =
            "USER CREATE;<firstName>;<surnames>;<dni>;<email>;<password>;<phone>;<card>";
    static final String USER_DELETE_SYNTAX = "USER DELETE;<email>;<password>";
    static final String USER_UPDATE_SYNTAX = "USER UPDATE;<email>;<password>;<field>;<value>";

    @FunctionalInterface
    private interface Handler {
        CommandResult execute(String[] arguments) throws ParkException;
    }

    /** A command keyword with every accepted syntax and the argument count of each. */
    private record Command(List<CommandInfo> infos, Handler handler) {

        boolean accepts(int argumentCount) {
            for (CommandInfo info : infos) {
                if (info.syntax().split(";", -1).length - 1 == argumentCount) {
                    return true;
                }
            }
            return false;
        }

        String usage() {
            List<String> syntaxes = new ArrayList<>();
            for (CommandInfo info : infos) {
                syntaxes.add(info.syntax());
            }
            return String.join(" or ", syntaxes);
        }
    }

    private final Park park;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private final InvoiceFormatter invoiceFormatter = new InvoiceFormatter();
    private final Map<String, Command> commands = new LinkedHashMap<>();

    /** Uses the standard HELP and HELP EXTENDED layouts. */
    public CommandProcessor(Park park) {
        this(park, new HelpGenerator(new SyntaxHelpFormatter()), new HelpGenerator(new ExtendedHelpFormatter()));
    }

    /**
     * @param help         generator used by HELP
     * @param extendedHelp generator used by HELP EXTENDED
     */
    public CommandProcessor(Park park, HelpGenerator help, HelpGenerator extendedHelp) {
        this.park = park;
        this.help = help;
        this.extendedHelp = extendedHelp;
        registerUserCommands();
        registerSystemCommands();
        registerGroupCommands();
        registerTicketCommands();
        registerPassCommands();
        registerAttractionCommands();
        registerInvoiceCommands();
        register("HELP", "Lists the syntax of every command.",
                a -> CommandResult.of(help.generate(commandInfos())));
        register("HELP EXTENDED", "Lists every command with an explanation of its purpose.",
                a -> CommandResult.of(extendedHelp.generate(commandInfos())));
        register("EXIT", "Ends the application; later commands in a file are not run.",
                a -> new CommandResult("Bye", true));
    }

    public CommandResult process(String line) {
        if (line == null || line.trim().isEmpty()) {
            return CommandResult.of("");
        }
        String[] parts = line.split(";", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        Command command = commands.get(parts[0].toUpperCase(Locale.ROOT));
        if (command == null) {
            return CommandResult.error("Unknown command '" + parts[0] + "'. Type HELP to list commands");
        }
        if (!command.accepts(parts.length - 1)) {
            return CommandResult.error("Wrong number of arguments. Usage: " + command.usage());
        }
        try {
            return command.handler().execute(Arrays.copyOfRange(parts, 1, parts.length));
        } catch (ParkException e) {
            return CommandResult.error(e.getMessage());
        }
    }

    private void registerUserCommands() {
        register(USER_CREATE_SYNTAX,
                "Registers a new user after validating DNI, email, password and card.", a -> {
            park.createUser(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
            return CommandResult.of("USER CREATED");
        });
        register(USER_DELETE_SYNTAX,
                "Deletes a user identified by email and password, if no active ticket or locked group prevents it.", a -> {
            park.deleteUser(a[0], a[1]);
            return CommandResult.of("USER DELETED");
        });
        register(USER_UPDATE_SYNTAX,
                "Changes one field of a user (NAME, SURNAME, EMAIL, PASSWORD, PHONE or CARD); the DNI cannot change.", a -> {
            UserField field = UserField.fromString(a[2]);
            if (field == null) {
                return CommandResult.error("Field '" + a[2] + "' cannot be modified. Allowed fields: "
                        + UserField.allNames());
            }
            park.updateUser(a[0], a[1], field, a[3]);
            return CommandResult.of("USER UPDATED");
        });
    }

    private void registerSystemCommands() {
        register("SYSTEM DATE;<yyyy-MM-dd>",
                "Sets the current system date used by every date-dependent operation.", a -> {
            park.setDate(a[0]);
            return CommandResult.of("SYSTEM DATE SET TO " + park.getDate());
        });
        register("SYSTEM PRICE;<price>",
                "Sets the base price of new tickets for the rest of this run (40.00 at start).", a -> {
            park.setPrice(a[0]);
            return CommandResult.of("SYSTEM PRICE SET TO " + Park.formatAmount(park.getPrice()) + " EUR");
        });
    }

    private void registerGroupCommands() {
        register("GROUP CREATE;<identifier>;<name>",
                "Creates an empty group of users.", a -> {
            park.createGroup(a[0], a[1]);
            return CommandResult.of("GROUP CREATED");
        });
        register("GROUP ADD;<identifier>;<email>",
                "Adds an existing user to a group that has no active group ticket.", a -> {
            park.addToGroup(a[0], a[1]);
            return CommandResult.of("USER ADDED TO GROUP");
        });
        register("GROUP REMOVE;<identifier>;<email>",
                "Removes a member from a group that has no active group ticket.", a -> {
            park.removeFromGroup(a[0], a[1]);
            return CommandResult.of("USER REMOVED FROM GROUP");
        });
        register("GROUP DELETE;<identifier>",
                "Deletes a group that has no active group ticket; its historical tickets are kept.", a -> {
            park.deleteGroup(a[0]);
            return CommandResult.of("GROUP DELETED");
        });
    }

    private void registerTicketCommands() {
        register("TICKET CREATE;<identifier>;<email>;<date>",
                "Buys an individual ticket for a user, valid on the given date, at the current price.", a -> {
            Ticket ticket = park.createTicket(a[0], a[1], a[2]);
            return CommandResult.of("TICKET CREATED. Price: " + Park.formatAmount(ticket.getPrice()) + " EUR");
        });
        register("TICKET DELETE;<identifier>",
                "Refunds a standalone individual ticket dated after today, together with its passes.", a -> {
            park.deleteTicket(a[0]);
            return CommandResult.of("TICKET DELETED");
        });
        register("GROUPTICKET CREATE;<identifier>;<group>;<date>",
                "Buys one ticket per group member, with the group discount applied.", a -> {
            GroupTicket ticket = park.createGroupTicket(a[0], a[1], a[2]);
            return CommandResult.of("GROUPTICKET CREATED. Tickets: " + ticket.getComposition().size()
                    + ", discount: " + ticket.getDiscountPercent() + "%, total: "
                    + Park.formatAmount(ticket.getPrice()) + " EUR");
        });
        register("GROUPTICKET DELETE;<identifier>",
                "Refunds a group ticket dated after today, with its individual tickets and their passes.", a -> {
            park.deleteGroupTicket(a[0]);
            return CommandResult.of("GROUPTICKET DELETED");
        });
    }

    private void registerPassCommands() {
        register("SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>",
                "Buys 1 or 10 SpeedPasses for an individual ticket dated today or later; each costs 20% of the ticket.",
                a -> {
                    List<SpeedPass> bought = park.buySpeedPasses(a[0], a[1], a[2]);
                    BigDecimal total = BigDecimal.ZERO;
                    for (SpeedPass pass : bought) {
                        total = total.add(pass.getPrice());
                    }
                    String ids = bought.size() == 1 ? bought.get(0).getId()
                            : bought.get(0).getId() + " to " + bought.get(bought.size() - 1).getId();
                    return CommandResult.of("SPEEDPASS CREATED. Passes: " + ids + ", unit price: "
                            + Park.formatAmount(bought.get(0).getPrice()) + " EUR, total: "
                            + Park.formatAmount(total) + " EUR");
                });
        register("SPEEDPASS DELETE;<speedPassIdentifier>",
                "Refunds an unconsumed SpeedPass whose ticket is dated after today.", a -> {
                    park.refundSpeedPass(a[0]);
                    return CommandResult.of("SPEEDPASS DELETED");
                });
        register("SPECIALPASS CREATE;<identifier>;<individualTicket>",
                "Buys the single Special Pass of an individual ticket dated today or later, at the ticket's price.",
                a -> {
                    SpecialPass pass = park.buySpecialPass(a[0], a[1]);
                    return CommandResult.of("SPECIALPASS CREATED. Price: " + Park.formatAmount(pass.getPrice())
                            + " EUR");
                });
        register("SPECIALPASS DELETE;<specialPassIdentifier>",
                "Refunds an unconsumed Special Pass whose ticket is dated after today.", a -> {
                    park.refundSpecialPass(a[0]);
                    return CommandResult.of("SPECIALPASS DELETED");
                });
    }

    private void registerAttractionCommands() {
        register(a -> {
            park.createAttraction(a[0], a[1], a[2], a.length == 4 ? a[3] : null);
            return CommandResult.of("ATTRACTION CREATED");
        }, new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>",
                "Creates a GENERAL attraction with the given capacity."),
                new CommandInfo("ATTRACTION CREATE;<identifier>;<name>;<capacity>;<type>",
                        "Creates an attraction of type GENERAL, INDIVIDUAL, GROUP or SPECIAL."));
        register(a -> CommandResult.of(entered(park.enterAttraction(a[0], a[1], a.length == 3 ? a[2] : null))),
                new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>",
                        "Lets the holders of a ticket valid today enter together, if the attraction type accepts the "
                                + "ticket; special attractions use the ticket's Special Pass."),
                new CommandInfo("ATTRACTION ENTER;<attraction>;<ticket>;SPEED",
                        "Enters using one unconsumed SpeedPass per entering individual ticket (not at special "
                                + "attractions)."));
        register("ATTRACTION EXIT;<attraction>;<email>",
                "Takes a user out of an attraction, freeing one place.", a -> {
            park.exitAttraction(a[0], a[1]);
            return CommandResult.of("ATTRACTION EXITED");
        });
        register("ATTRACTION LIST;<attraction>",
                "Lists the emails of the users inside an attraction.", a -> {
            Attraction<?> attraction = park.requireAttraction(a[0]);
            if (attraction.getOccupants().isEmpty()) {
                return CommandResult.of("NO USERS IN ATTRACTION " + attraction.getId());
            }
            return CommandResult.of(String.join(System.lineSeparator(), emails(attraction)));
        });
        register("ATTRACTION STATUS;<attraction>",
                "Shows identifier, name, type, capacity, occupancy, free places and users of an attraction.",
                a -> CommandResult.of(status(park.requireAttraction(a[0]))));
    }

    private void registerInvoiceCommands() {
        register(a -> {
            Invoice invoice = switch (a[0].toUpperCase(Locale.ROOT)) {
                case "USER" -> park.userInvoice(a[1]);
                case "GROUP" -> park.groupInvoice(a[1]);
                default -> throw new InvalidDataException("Invoices can be created for USER or GROUP, not '"
                        + a[0] + "'");
            };
            return CommandResult.of(invoiceFormatter.format(invoice));
        }, new CommandInfo("INVOICE CREATE;USER;<email>",
                "Shows the invoice of a user: direct tickets and personal passes, with the total."),
                new CommandInfo("INVOICE CREATE;GROUP;<groupIdentifier>",
                        "Shows the invoice of a group: its group tickets with their composition, with the total."));
    }

    private static String entered(EntryResult result) {
        StringBuilder text = new StringBuilder("ATTRACTION ENTERED (").append(result.users())
                .append(result.users() == 1 ? " user)" : " users)");
        List<String> speed = new ArrayList<>();
        for (Pass pass : result.consumed()) {
            if (pass.getType() == PassType.SPECIAL) {
                text.append(". Special Pass consumed: ").append(pass.getId());
            } else {
                speed.add(pass.getId());
            }
        }
        if (!speed.isEmpty()) {
            text.append(speed.size() == 1 ? ". SpeedPass consumed: " : ". SpeedPasses consumed: ")
                    .append(String.join(", ", speed));
        }
        return text.toString();
    }

    private static String status(Attraction<?> attraction) {
        List<String> emails = emails(attraction);
        return String.join(System.lineSeparator(),
                "Attraction: " + attraction.getId(),
                "Name: " + attraction.getName(),
                "Type: " + attraction.getType(),
                "Capacity: " + attraction.getCapacity(),
                "Users inside: " + emails.size(),
                "Free places: " + attraction.getFreePlaces(),
                "Users: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
    }

    private static List<String> emails(Attraction<?> attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        return emails;
    }

    private void register(String syntax, String description, Handler handler) {
        register(handler, new CommandInfo(syntax, description));
    }

    private void register(Handler handler, CommandInfo... infos) {
        String keyword = infos[0].syntax().split(";", -1)[0];
        commands.put(keyword, new Command(List.of(infos), handler));
    }

    /** Every syntax of every command, in registration order. */
    List<CommandInfo> commandInfos() {
        List<CommandInfo> infos = new ArrayList<>();
        for (Command command : commands.values()) {
            infos.addAll(command.infos());
        }
        return infos;
    }
}
