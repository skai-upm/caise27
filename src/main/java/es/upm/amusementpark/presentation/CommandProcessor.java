package es.upm.amusementpark.presentation;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.ParkController;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.EntryResult;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.validation.ValueParser;

/**
 * Interprets a single ';'-separated command line, delegates to the business controllers and prints
 * the result. Business exceptions are reported here so that processing always continues.
 */
public class CommandProcessor {

    /** Handler for one command; receives the arguments after the command keyword. */
    @FunctionalInterface
    private interface Handler {
        void handle(String[] args);
    }

    private record Command(int minArgs, int maxArgs, Handler handler) {
    }

    private static final String EXIT = "EXIT";

    private final ParkController park;
    private final PassController passes;
    private final InvoiceController invoices;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private final PrintStream out;
    private final Map<String, Command> commands = new HashMap<>();

    public CommandProcessor(ParkController park, PassController passes, InvoiceController invoices,
                            HelpGenerator help, HelpGenerator extendedHelp, PrintStream out) {
        this.park = park;
        this.passes = passes;
        this.invoices = invoices;
        this.help = help;
        this.extendedHelp = extendedHelp;
        this.out = out;
        registerUserCommands();
        registerSystemAndGroupCommands();
        registerTicketCommands();
        registerAttractionCommands();
        registerPassAndInvoiceCommands();
        register("HELP", 0, a -> out.println(this.help.generate()));
        register("HELP EXTENDED", 0, a -> out.println(this.extendedHelp.generate()));
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} if the application must stop (EXIT), {@code true} otherwise
     */
    public boolean process(String line) {
        String[] parts = line.split(";", -1);
        String keyword = parts[0].trim();
        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);
        try {
            if (EXIT.equals(keyword)) {
                requireArgs(args, 0, 0);
                out.println(EXIT);
                return false;
            }
            Command command = commands.get(keyword);
            if (command == null) {
                throw new ValidationException("unknown command: " + keyword + " (type HELP for syntax)");
            }
            requireArgs(args, command.minArgs(), command.maxArgs());
            command.handler().handle(args);
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR: could not process command: " + e.getMessage());
        }
        return true;
    }

    private void register(String keyword, int args, Handler handler) {
        commands.put(keyword, new Command(args, args, handler));
    }

    private void register(String keyword, int minArgs, int maxArgs, Handler handler) {
        commands.put(keyword, new Command(minArgs, maxArgs, handler));
    }

    private void registerUserCommands() {
        register("USER CREATE", 7, a -> {
            park.createUser(a[0], a[1], a[2], a[3], a[4], a[5], a[6]);
            out.println("USER CREATED");
        });
        register("USER DELETE", 2, a -> {
            park.deleteUser(a[0], a[1]);
            out.println("USER DELETED");
        });
        register("USER UPDATE", 4, a -> {
            park.updateUser(a[0], a[1], a[2], a[3]);
            out.println("USER UPDATED");
        });
    }

    private void registerSystemAndGroupCommands() {
        register("SYSTEM DATE", 1, a -> {
            park.setCurrentDate(ValueParser.parseDate(a[0]));
            out.println("SYSTEM DATE SET;" + park.getCurrentDate());
        });
        register("SYSTEM PRICE", 1, a -> {
            park.setPrice(ValueParser.parsePrice(a[0]));
            out.println("SYSTEM PRICE SET;" + money(park.getPrice()));
        });
        register("GROUP CREATE", 2, a -> {
            park.createGroup(a[0], a[1]);
            out.println("GROUP CREATED");
        });
        register("GROUP ADD", 2, a -> {
            park.addToGroup(a[0], a[1]);
            out.println("GROUP USER ADDED");
        });
        register("GROUP REMOVE", 2, a -> {
            park.removeFromGroup(a[0], a[1]);
            out.println("GROUP USER REMOVED");
        });
        register("GROUP DELETE", 1, a -> {
            park.deleteGroup(a[0]);
            out.println("GROUP DELETED");
        });
    }

    private void registerTicketCommands() {
        register("TICKET CREATE", 3, a ->
                out.println("TICKET CREATED;PRICE=" + money(park.createTicket(a[0], a[1], a[2]).getPrice())));
        register("TICKET DELETE", 1, a -> {
            park.deleteTicket(a[0]);
            out.println("TICKET DELETED");
        });
        register("GROUPTICKET CREATE", 3, a ->
                out.println("GROUPTICKET CREATED;TOTAL=" + money(park.createGroupTicket(a[0], a[1], a[2]).getPrice())));
        register("GROUPTICKET DELETE", 1, a -> {
            park.deleteGroupTicket(a[0]);
            out.println("GROUPTICKET DELETED");
        });
    }

    private void registerAttractionCommands() {
        register("ATTRACTION CREATE", 3, 4, a -> {
            park.createAttraction(a[0], a[1], a[2], a.length == 4 ? a[3] : "GENERAL");
            out.println("ATTRACTION CREATED");
        });
        register("ATTRACTION ENTER", 2, 3, a -> {
            if (a.length == 3 && !"SPEED".equals(a[2])) {
                throw new ValidationException("unknown entry option: " + a[2] + " (expected SPEED)");
            }
            printEntry(park.enterAttraction(a[0], a[1], a.length == 3));
        });
        register("ATTRACTION EXIT", 2, a -> {
            park.exitAttraction(a[0], a[1]);
            out.println("ATTRACTION EXITED");
        });
        register("ATTRACTION LIST", 1, a -> printOccupants(park.requireAttraction(a[0])));
        register("ATTRACTION STATUS", 1, a -> printStatus(park.requireAttraction(a[0])));
    }

    private void registerPassAndInvoiceCommands() {
        register("SPEEDPASS CREATE", 3, a -> {
            List<SpeedPass> bought = passes.buySpeedPasses(a[0], a[1], a[2]);
            out.println("SPEEDPASS CREATED;" + bought.size() + ";TOTAL=" + money(PassController.total(bought)));
        });
        register("SPEEDPASS DELETE", 1, a -> {
            passes.refundSpeedPass(a[0]);
            out.println("SPEEDPASS DELETED");
        });
        register("SPECIALPASS CREATE", 2, a -> {
            SpecialPass pass = passes.buySpecialPass(a[0], a[1]);
            out.println("SPECIALPASS CREATED;PRICE=" + money(pass.getPrice()));
        });
        register("SPECIALPASS DELETE", 1, a -> {
            passes.refundSpecialPass(a[0]);
            out.println("SPECIALPASS DELETED");
        });
        register("INVOICE CREATE", 2, a -> out.println(invoices.create(a[0], a[1]).render()));
    }

    private void printEntry(EntryResult result) {
        StringBuilder sb = new StringBuilder("ATTRACTION ENTERED;" + result.entered().size());
        for (Pass pass : result.consumedPasses()) {
            sb.append(';').append(pass.getKind()).append(" CONSUMED=").append(pass.getId());
        }
        out.println(sb);
    }

    private void printOccupants(Attraction<?> attraction) {
        List<User> occupants = attraction.getOccupants();
        if (occupants.isEmpty()) {
            out.println("NO USERS");
            return;
        }
        occupants.forEach(u -> out.println(u.getEmail()));
    }

    private void printStatus(Attraction<?> a) {
        out.println("ATTRACTION;" + a.getId());
        out.println("NAME;" + a.getName());
        out.println("TYPE;" + a.getType());
        out.println("CAPACITY;" + a.getCapacity());
        out.println("INSIDE;" + a.getOccupants().size());
        out.println("AVAILABLE;" + a.getAvailable());
        out.println("USERS;" + a.getOccupants().stream().map(User::getEmail).collect(Collectors.joining(",")));
    }

    private static String money(BigDecimal amount) {
        return amount.setScale(2).toPlainString();
    }

    private static void requireArgs(String[] args, int min, int max) {
        if (args.length < min || args.length > max) {
            String expected = min == max ? String.valueOf(min) : min + " to " + max;
            throw new ValidationException("expected " + expected + " arguments but got " + args.length
                    + " (type HELP for syntax)");
        }
    }
}
