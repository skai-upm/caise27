package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.InvoiceDirector;

import es.upm.amusementpark.business.control.InvoiceController;
import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.Entry;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes text commands one line at a time. The same instance and logic are
 * used for every input source (standard input or a command file).
 */
public class CommandProcessor {

    private final PrintStream out;
    private final ParkController park;
    private final InvoiceController invoices;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;

    private Runnable afterCommand = () -> { };

    /** Processor using the controllers injected by the application's initialization. */
    public CommandProcessor(PrintStream out, ParkController park, InvoiceController invoices,
            HelpGenerator help, HelpGenerator extendedHelp) {
        this.out = out;
        this.park = park;
        this.invoices = invoices;
        this.help = help;
        this.extendedHelp = extendedHelp;
    }

    /** Convenience wiring to the singleton controllers and the standard help generators. */
    public CommandProcessor(PrintStream out) {
        this(out, ParkController.getInstance(), InvoiceController.getInstance(),
                new HelpGenerator(CommandCatalog.standard(), new SyntaxHelpFormatter()),
                new HelpGenerator(CommandCatalog.standard(), new ExtendedHelpFormatter()));
        invoices.configure(park, new InvoiceDirector());
    }

    /** Action run after every processed line (for example, saving the park state). */
    public void setAfterCommand(Runnable afterCommand) {
        this.afterCommand = afterCommand;
    }

    /**
     * Processes every line of the given source until the end of the input or
     * until an EXIT command is executed.
     */
    public void run(BufferedReader in) throws IOException {
        String line;
        while ((line = in.readLine()) != null) {
            boolean keepGoing;
            try {
                keepGoing = execute(line);
            } catch (RuntimeException e) {
                out.println("Error: could not process command: " + line.trim());
                keepGoing = true;
            }
            afterCommand.run();
            if (!keepGoing) {
                return;
            }
        }
    }

    /**
     * Executes a single command line. Fields are separated by ';' and the first
     * field is the command keyword.
     *
     * @return {@code false} when processing must stop (EXIT), {@code true} otherwise
     */
    public boolean execute(String line) {
        String command = line.trim();
        if (command.isEmpty()) {
            return true;
        }
        String[] fields = command.split(";", -1);
        String keyword = fields[0].trim();
        try {
            return dispatch(command, keyword, fields);
        } catch (ParkException | InvalidCommandException e) {
            out.println("Error: " + e.getMessage());
            return true;
        }
    }

    private boolean dispatch(String command, String keyword, String[] fields) {
        switch (keyword) {
            case "HELP":
                if (rejectArguments(keyword, fields)) {
                    return true;
                }
                out.println(help.generate());
                return true;
            case "HELP EXTENDED":
                if (rejectArguments(keyword, fields)) {
                    return true;
                }
                out.println(extendedHelp.generate());
                return true;
            case "EXIT":
                if (rejectArguments(keyword, fields)) {
                    return true;
                }
                out.println("Exiting application.");
                return false;
            case "USER CREATE":
                if (requireArguments(keyword, fields, 7)) {
                    park.createUser(fields[1], fields[2], fields[3], fields[4], fields[5], fields[6], fields[7]);
                    out.println("USER CREATED");
                }
                return true;
            case "SYSTEM DATE":
                if (requireArguments(keyword, fields, 1)) {
                    park.setCurrentDate(fields[1]);
                    out.println("SYSTEM DATE UPDATED: " + park.getCurrentDate());
                }
                return true;
            case "SYSTEM PRICE":
                if (requireArguments(keyword, fields, 1)) {
                    park.setBasePrice(fields[1]);
                    out.println("SYSTEM PRICE UPDATED: " + park.getBasePrice());
                }
                return true;
            case "GROUP CREATE":
                if (requireArguments(keyword, fields, 2)) {
                    park.createGroup(fields[1], fields[2]);
                    out.println("GROUP CREATED");
                }
                return true;
            case "GROUP ADD":
                if (requireArguments(keyword, fields, 2)) {
                    park.addUserToGroup(fields[1], fields[2]);
                    out.println("USER ADDED TO GROUP");
                }
                return true;
            case "GROUP REMOVE":
                if (requireArguments(keyword, fields, 2)) {
                    park.removeUserFromGroup(fields[1], fields[2]);
                    out.println("USER REMOVED FROM GROUP");
                }
                return true;
            case "GROUP DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.deleteGroup(fields[1]);
                    out.println("GROUP DELETED");
                }
                return true;
            case "TICKET CREATE":
                if (requireArguments(keyword, fields, 3)) {
                    Ticket ticket = park.createIndividualTicket(fields[1], fields[2], fields[3]);
                    out.println("TICKET CREATED. PRICE: " + ticket.getPrice() + " EUR");
                }
                return true;
            case "TICKET DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.deleteTicket(fields[1]);
                    out.println("TICKET DELETED");
                }
                return true;
            case "GROUPTICKET CREATE":
                if (requireArguments(keyword, fields, 3)) {
                    Ticket ticket = park.createGroupTicket(fields[1], fields[2], fields[3]);
                    out.println("GROUP TICKET CREATED. TOTAL PRICE: " + ticket.getPrice() + " EUR");
                }
                return true;
            case "GROUPTICKET DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.deleteGroupTicket(fields[1]);
                    out.println("GROUP TICKET DELETED");
                }
                return true;
            case "ATTRACTION CREATE":
                if (fields.length == 5) {
                    park.createAttraction(fields[1], fields[2], fields[3], fields[4]);
                } else if (requireArguments(keyword, fields, 3)) {
                    park.createAttraction(fields[1], fields[2], fields[3]);
                }
                out.println("ATTRACTION CREATED");
                return true;
            case "ATTRACTION ENTER":
                boolean speed = fields.length == 4 && fields[3].trim().equals("SPEED");
                if (speed || requireArguments(keyword, fields, 2)) {
                    printEntry(park.enterAttraction(fields[1], fields[2], speed), fields[1].trim());
                }
                return true;
            case "ATTRACTION EXIT":
                if (requireArguments(keyword, fields, 2)) {
                    User user = park.exitAttraction(fields[1], fields[2]);
                    out.println("USER " + user.getEmail() + " LEFT ATTRACTION " + fields[1].trim());
                }
                return true;
            case "ATTRACTION STATUS":
                if (requireArguments(keyword, fields, 1)) {
                    printStatus(park.existingAttraction(fields[1]));
                }
                return true;
            case "ATTRACTION LIST":
                if (requireArguments(keyword, fields, 1)) {
                    listOccupants(park.existingAttraction(fields[1]));
                }
                return true;
            case "SPEEDPASS CREATE":
                if (requireArguments(keyword, fields, 3)) {
                    List<SpeedPass> bought = park.buySpeedPasses(fields[1], fields[2], fields[3]);
                    BigDecimal total = BigDecimal.ZERO.setScale(2);
                    for (SpeedPass pass : bought) {
                        total = total.add(pass.getPrice());
                    }
                    out.println("SPEEDPASS PURCHASED (" + bought.size() + "). TOTAL PRICE: " + total + " EUR");
                }
                return true;
            case "SPEEDPASS DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.refundSpeedPass(fields[1]);
                    out.println("SPEEDPASS REFUNDED");
                }
                return true;
            case "SPECIALPASS CREATE":
                if (requireArguments(keyword, fields, 2)) {
                    SpecialPass pass = park.buySpecialPass(fields[1], fields[2]);
                    out.println("SPECIALPASS PURCHASED. PRICE: " + pass.getPrice() + " EUR");
                }
                return true;
            case "SPECIALPASS DELETE":
                if (requireArguments(keyword, fields, 1)) {
                    park.refundSpecialPass(fields[1]);
                    out.println("SPECIALPASS REFUNDED");
                }
                return true;
            case "INVOICE CREATE":
                if (requireArguments(keyword, fields, 2)) {
                    out.println(createInvoice(fields[1].trim(), fields[2]).toText());
                }
                return true;
            default:
                throw new InvalidCommandException("unknown command: " + command);
        }
    }

    private Invoice createInvoice(String target, String identifier) {
        switch (target) {
            case "USER":
                return invoices.createUserInvoice(identifier);
            case "GROUP":
                return invoices.createGroupInvoice(identifier);
            default:
                throw new InvalidCommandException("invalid invoice target (expected USER or GROUP): " + target);
        }
    }

    private void printEntry(Entry entry, String attractionIdentifier) {
        for (User user : entry.entrants()) {
            out.println("USER " + user.getEmail() + " ENTERED ATTRACTION " + attractionIdentifier);
        }
        for (Pass pass : entry.consumedPasses()) {
            String kind = pass instanceof SpecialPass ? "SPECIALPASS " : "SPEEDPASS ";
            out.println(kind + pass.getIdentifier() + " CONSUMED");
        }
    }

    private void printStatus(Attraction<?> attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        out.println("ATTRACTION: " + attraction.getIdentifier());
        out.println("NAME: " + attraction.getName());
        out.println("TYPE: " + attraction.getType());
        out.println("CAPACITY: " + attraction.getCapacity());
        out.println("USERS INSIDE: " + emails.size());
        out.println("AVAILABLE PLACES: " + attraction.getAvailablePlaces());
        out.println("OCCUPANTS: " + (emails.isEmpty() ? "none" : String.join(", ", emails)));
    }

    private void listOccupants(Attraction<?> attraction) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("NO USERS IN ATTRACTION " + attraction.getIdentifier());
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
        }
    }

    /** @throws InvalidCommandException unless the command has exactly {@code expected} arguments */
    private static boolean requireArguments(String keyword, String[] fields, int expected) {
        if (fields.length != expected + 1) {
            throw new InvalidCommandException("invalid arguments for " + keyword + " (expected " + expected + ")");
        }
        return true;
    }

    /** @throws InvalidCommandException if the command has any argument */
    private static boolean rejectArguments(String keyword, String[] fields) {
        if (fields.length != 1) {
            throw new InvalidCommandException("invalid arguments for " + keyword + " (expected no arguments)");
        }
        return false;
    }
}
