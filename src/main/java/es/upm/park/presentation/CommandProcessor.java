package es.upm.park.presentation;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import es.upm.park.business.controller.Dates;
import es.upm.park.business.controller.ParkController;
import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.User;
import es.upm.park.business.service.PassService;
import es.upm.park.persistence.PersistenceException;
import es.upm.park.presentation.help.CommandCatalog;
import es.upm.park.presentation.help.ExtendedHelpFormatter;
import es.upm.park.presentation.help.HelpGenerator;
import es.upm.park.presentation.help.SyntaxHelpFormatter;
import es.upm.park.business.service.ParkException;

/**
 * Interprets one text command (fields separated by ';') and returns the
 * message to show. Never throws for incorrect input.
 */
public class CommandProcessor {

    static final String BYE = "Bye";

    /** A command handler: expected number of fields and the action on them. */
    private record Command(int minFields, int maxFields, Function<String[], String> action) {

        Command(int fields, Function<String[], String> action) {
            this(fields, fields, action);
        }

        boolean accepts(int fields) {
            return fields >= minFields && fields <= maxFields;
        }

        String expected() {
            return minFields == maxFields ? String.valueOf(minFields) : minFields + " to " + maxFields;
        }
    }

    private final ParkController park;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private boolean exitRequested;

    /** Processor with the default syntax and extended help generators. */
    public CommandProcessor(ParkController park) {
        this(park, new HelpGenerator(new SyntaxHelpFormatter(), CommandCatalog.commands()),
                new HelpGenerator(new ExtendedHelpFormatter(), CommandCatalog.commands()));
    }

    /** Processor whose help output is produced by the injected generators. */
    public CommandProcessor(ParkController park, HelpGenerator help, HelpGenerator extendedHelp) {
        this.park = park;
        this.help = help;
        this.extendedHelp = extendedHelp;
        commands.put("USER CREATE", new Command(7, this::createUser));
        commands.put("USER DELETE", new Command(2, this::deleteUser));
        commands.put("USER UPDATE", new Command(4, this::updateUser));
        commands.put("GROUP CREATE", new Command(2, this::createGroup));
        commands.put("GROUP ADD", new Command(2, this::addToGroup));
        commands.put("GROUP REMOVE", new Command(2, this::removeFromGroup));
        commands.put("GROUP DELETE", new Command(1, this::deleteGroup));
        commands.put("TICKET CREATE", new Command(3, this::createTicket));
        commands.put("TICKET DELETE", new Command(1, this::deleteTicket));
        commands.put("GROUPTICKET CREATE", new Command(3, this::createGroupTicket));
        commands.put("GROUPTICKET DELETE", new Command(1, this::deleteGroupTicket));
        commands.put("SPEEDPASS CREATE", new Command(3, this::buySpeedPasses));
        commands.put("SPEEDPASS DELETE", new Command(1,
                f -> "SpeedPass refunded: " + park.getPassService().refundSpeedPass(f[0]).getId()));
        commands.put("SPECIALPASS CREATE", new Command(2, this::buySpecialPass));
        commands.put("SPECIALPASS DELETE", new Command(1,
                f -> "Special pass refunded: " + park.getPassService().refundSpecialPass(f[0]).getId()));
        commands.put("ATTRACTION CREATE", new Command(3, 4, this::createAttraction));
        commands.put("ATTRACTION LIST", new Command(1, this::listAttraction));
        commands.put("ATTRACTION ENTER", new Command(2, this::enterAttraction));
        commands.put("ATTRACTION EXIT", new Command(2,
                f -> "Exited " + f[0] + ": " + park.getAttractionService().exit(f[0], f[1]).getEmail()));
        commands.put("ATTRACTION STATUS", new Command(1, f -> park.getAttractionService().status(f[0])));
        commands.put("INVOICE CREATE", new Command(2, this::createInvoice));
        commands.put("SYSTEM DATE", new Command(1, f -> "System date set to " + park.setCurrentDate(f[0])));
        commands.put("SYSTEM PRICE", new Command(1,
                f -> "System price set to " + park.setBasePrice(f[0]).toPlainString()));
        commands.put("HELP", new Command(0, f -> help.generate()));
        commands.put("HELP EXTENDED", new Command(0, f -> extendedHelp.generate()));
        commands.put("EXIT", new Command(0, this::exit));
    }

    /**
     * Processes a command line.
     *
     * @return the message to print, or {@code null} for a blank line
     */
    public String process(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] parts = line.split(";", -1);
        String name = parts[0].trim();
        Command command = commands.get(name);
        if (command == null) {
            return error("unknown command '" + name + "'");
        }
        if (!command.accepts(parts.length - 1)) {
            return error(name + " expects " + command.expected() + " fields");
        }
        try {
            return command.action().apply(Arrays.copyOfRange(parts, 1, parts.length));
        } catch (ParkException | PersistenceException e) {
            return error(e.getMessage());
        } catch (RuntimeException e) {
            return error("command could not be processed");
        }
    }

    /**
     * Whether an EXIT command has been processed; no further command must
     * be processed afterwards.
     */
    public boolean isExitRequested() {
        return exitRequested;
    }

    private String createUser(String[] f) {
        park.getUserService().create(f[0], f[1], f[2], f[3], f[4], f[5], f[6]);
        return "User created: " + f[2];
    }

    private String deleteUser(String[] f) {
        park.deleteUser(f[0], f[1]);
        return "User deleted: " + f[0];
    }

    private String updateUser(String[] f) {
        String dni = park.getUserService().update(f[0], f[1], f[2], f[3]).getDni();
        return "User updated: " + dni + " " + f[2];
    }

    private String createGroup(String[] f) {
        return "Group created: " + park.getGroupService().create(f[0], f[1]).getId();
    }

    private String addToGroup(String[] f) {
        park.getGroupService().addUser(f[0], f[1]);
        return "User " + f[1] + " added to group " + f[0];
    }

    private String removeFromGroup(String[] f) {
        park.getGroupService().removeUser(f[0], f[1]);
        return "User " + f[1] + " removed from group " + f[0];
    }

    private String deleteGroup(String[] f) {
        park.deleteGroup(f[0]);
        return "Group deleted: " + f[0];
    }

    private String createTicket(String[] f) {
        BigDecimal price = park.getTicketService().createIndividual(f[0], f[1], Dates.parse(f[2])).getPrice();
        return "Ticket created: " + f[0] + " price " + price.toPlainString();
    }

    private String deleteTicket(String[] f) {
        park.deleteTicket(f[0]);
        return "Ticket deleted: " + f[0];
    }

    private String createGroupTicket(String[] f) {
        BigDecimal total = park.createGroupTicket(f[0], f[1], Dates.parse(f[2])).getPrice();
        return "Group ticket created: " + f[0] + " total " + total.toPlainString();
    }

    private String deleteGroupTicket(String[] f) {
        park.deleteGroupTicket(f[0]);
        return "Group ticket deleted: " + f[0];
    }

    private String buySpeedPasses(String[] f) {
        List<SpeedPass> passes = park.getPassService().buySpeedPasses(f[0], f[1], f[2]);
        String range = passes.size() == 1 ? passes.get(0).getId()
                : passes.get(0).getId() + ".." + passes.get(passes.size() - 1).getId();
        return "SpeedPasses created: " + range + " total " + PassService.total(passes).toPlainString();
    }

    private String buySpecialPass(String[] f) {
        SpecialPass pass = park.getPassService().buySpecialPass(f[0], f[1]);
        return "Special pass created: " + pass.getId() + " price " + pass.getPrice().toPlainString();
    }

    private String createAttraction(String[] f) {
        Attraction<?> attraction = f.length == 4
                ? park.getAttractionService().create(f[0], f[1], f[2], f[3])
                : park.getAttractionService().create(f[0], f[1], f[2]);
        return "Attraction created: " + attraction.getId();
    }

    private String listAttraction(String[] f) {
        List<String> emails = park.getAttractionService().occupantEmails(f[0]);
        if (emails.isEmpty()) {
            return "Attraction " + f[0] + " has no users inside";
        }
        return "Users in " + f[0] + ":" + System.lineSeparator() + String.join(System.lineSeparator(), emails);
    }

    private String enterAttraction(String[] f) {
        List<String> emails = park.enterAttraction(f[0], f[1]).stream().map(User::getEmail).toList();
        return "Entered " + f[0] + ": " + String.join(", ", emails);
    }

    private String createInvoice(String[] f) {
        return switch (f[0]) {
            case "USER" -> park.getInvoiceService().userInvoice(f[1]).render();
            case "GROUP" -> park.getInvoiceService().groupInvoice(f[1]).render();
            default -> error("unknown invoice type " + f[0]);
        };
    }

    private String exit(String[] f) {
        exitRequested = true;
        return BYE;
    }

    private static String error(String message) {
        return "Error: " + message;
    }
}
