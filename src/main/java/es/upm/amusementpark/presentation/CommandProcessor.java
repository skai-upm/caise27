package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.model.UserField;
import es.upm.amusementpark.business.model.attraction.Attraction;
import es.upm.amusementpark.business.model.attraction.EntryResult;
import es.upm.amusementpark.business.model.pass.Pass;
import es.upm.amusementpark.business.model.pass.SpeedPass;
import es.upm.amusementpark.business.model.ticket.Ticket;
import es.upm.amusementpark.business.validation.ValueParser;
import es.upm.amusementpark.presentation.help.HelpGenerator;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Interprets a single ';'-separated command line, executes it on the park and prints the outcome.
 * Every abnormal situation is caught here so that command processing never stops unexpectedly.
 */
public class CommandProcessor {

    private static final String SPEED = "SPEED";

    private final Park park;
    private final HelpGenerator help;
    private final HelpGenerator extendedHelp;
    private final InvoicePrinter invoicePrinter;
    private final PrintStream out;

    public CommandProcessor(Park park, HelpGenerator help, HelpGenerator extendedHelp,
                            InvoicePrinter invoicePrinter, PrintStream out) {
        this.park = park;
        this.help = help;
        this.extendedHelp = extendedHelp;
        this.invoicePrinter = invoicePrinter;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} if the command was EXIT and processing must stop, {@code true} otherwise
     */
    public boolean process(String line) {
        String[] p = line.trim().split(";", -1);
        try {
            return execute(p);
        } catch (ParkException | CommandSyntaxException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR: unexpected error: " + e);
        }
        return true;
    }

    private boolean execute(String[] p) throws ParkException, CommandSyntaxException {
        switch (p[0]) {
            case "USER CREATE" -> {
                requireFields(p, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>", 8);
                park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                out.println("USER CREATED");
            }
            case "USER DELETE" -> {
                requireFields(p, "USER DELETE;<Email>;<Password>", 3);
                park.deleteUser(p[1], p[2]);
                out.println("USER DELETED");
            }
            case "USER UPDATE" -> {
                requireFields(p, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>", 5);
                UserField field = UserField.fromName(p[3]);
                if (field == null) {
                    throw new CommandSyntaxException("field cannot be modified: " + p[3]);
                }
                park.updateUser(p[1], p[2], field, p[4]);
                out.println("USER UPDATED");
            }
            case "SYSTEM DATE" -> {
                requireFields(p, "SYSTEM DATE;<yyyy-MM-dd>", 2);
                park.setCurrentDate(p[1]);
                out.println("SYSTEM DATE SET: " + ValueParser.formatDate(park.getCurrentDate()));
            }
            case "SYSTEM PRICE" -> {
                requireFields(p, "SYSTEM PRICE;<Price>", 2);
                park.setCurrentPrice(p[1]);
                out.println("SYSTEM PRICE SET: " + money(park.getCurrentPrice()));
            }
            case "GROUP CREATE" -> {
                requireFields(p, "GROUP CREATE;<Identifier>;<Name>", 3);
                park.createGroup(p[1], p[2]);
                out.println("GROUP CREATED");
            }
            case "GROUP ADD" -> {
                requireFields(p, "GROUP ADD;<Identifier>;<Email>", 3);
                park.addToGroup(p[1], p[2]);
                out.println("GROUP USER ADDED");
            }
            case "GROUP REMOVE" -> {
                requireFields(p, "GROUP REMOVE;<Identifier>;<Email>", 3);
                park.removeFromGroup(p[1], p[2]);
                out.println("GROUP USER REMOVED");
            }
            case "GROUP DELETE" -> {
                requireFields(p, "GROUP DELETE;<Identifier>", 2);
                park.deleteGroup(p[1]);
                out.println("GROUP DELETED");
            }
            case "TICKET CREATE" -> {
                requireFields(p, "TICKET CREATE;<identifier>;<email>;<date>", 4);
                Ticket ticket = park.createTicket(p[1], p[2], p[3]);
                out.println("TICKET CREATED: price " + money(ticket.getPrice()));
            }
            case "TICKET DELETE" -> {
                requireFields(p, "TICKET DELETE;<identifier>", 2);
                park.deleteTicket(p[1]);
                out.println("TICKET DELETED");
            }
            case "GROUPTICKET CREATE" -> {
                requireFields(p, "GROUPTICKET CREATE;<identifier>;<group>;<date>", 4);
                Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                out.println("GROUPTICKET CREATED: total " + money(ticket.getPrice()));
            }
            case "GROUPTICKET DELETE" -> {
                requireFields(p, "GROUPTICKET DELETE;<identifier>", 2);
                park.deleteGroupTicket(p[1]);
                out.println("GROUPTICKET DELETED");
            }
            case "ATTRACTION CREATE" -> {
                requireFields(p, "ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]", 4, 5);
                park.createAttraction(p[1], p[2], p[3], p.length == 5 ? p[4] : null);
                out.println("ATTRACTION CREATED");
            }
            case "ATTRACTION ENTER" -> {
                requireFields(p, "ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]", 3, 4);
                if (p.length == 4 && !p[3].equals(SPEED)) {
                    throw new CommandSyntaxException("unknown entry option, expected SPEED: " + p[3]);
                }
                printEntry(park.enterAttraction(p[1], p[2], p.length == 4));
            }
            case "ATTRACTION EXIT" -> {
                requireFields(p, "ATTRACTION EXIT;<attraction>;<email>", 3);
                park.exitAttraction(p[1], p[2]);
                out.println("ATTRACTION EXITED");
            }
            case "ATTRACTION LIST" -> {
                requireFields(p, "ATTRACTION LIST;<attraction>", 2);
                printList(park.findAttraction(p[1]));
            }
            case "ATTRACTION STATUS" -> {
                requireFields(p, "ATTRACTION STATUS;<attractionIdentifier>", 2);
                printStatus(park.findAttraction(p[1]));
            }
            case "SPEEDPASS CREATE" -> {
                requireFields(p, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>", 4);
                List<SpeedPass> created = park.buySpeedPasses(p[1], p[2], p[3]);
                BigDecimal total = BigDecimal.ZERO.setScale(2);
                for (SpeedPass pass : created) {
                    total = total.add(pass.getPrice());
                }
                out.println("SPEEDPASS CREATED: " + created.size() + " pass(es), total " + money(total));
            }
            case "SPEEDPASS DELETE" -> {
                requireFields(p, "SPEEDPASS DELETE;<speedPassIdentifier>", 2);
                park.refundSpeedPass(p[1]);
                out.println("SPEEDPASS DELETED");
            }
            case "SPECIALPASS CREATE" -> {
                requireFields(p, "SPECIALPASS CREATE;<identifier>;<individualTicket>", 3);
                out.println("SPECIALPASS CREATED: price " + money(park.buySpecialPass(p[1], p[2]).getPrice()));
            }
            case "SPECIALPASS DELETE" -> {
                requireFields(p, "SPECIALPASS DELETE;<specialPassIdentifier>", 2);
                park.refundSpecialPass(p[1]);
                out.println("SPECIALPASS DELETED");
            }
            case "INVOICE CREATE" -> {
                requireFields(p, "INVOICE CREATE;USER|GROUP;<identifier>", 3);
                switch (p[1]) {
                    case "USER" -> invoicePrinter.print(park.userInvoice(p[2]), out);
                    case "GROUP" -> invoicePrinter.print(park.groupInvoice(p[2]), out);
                    default -> throw new CommandSyntaxException("invoice type must be USER or GROUP: " + p[1]);
                }
            }
            case "HELP" -> {
                requireFields(p, "HELP", 1);
                out.println(help.generate());
            }
            case "HELP EXTENDED" -> {
                requireFields(p, "HELP EXTENDED", 1);
                out.println(extendedHelp.generate());
            }
            case "EXIT" -> {
                requireFields(p, "EXIT", 1);
                return false;
            }
            default -> throw new CommandSyntaxException("unknown command: " + p[0]);
        }
        return true;
    }

    private void printEntry(EntryResult result) {
        StringBuilder line = new StringBuilder("ATTRACTION ENTERED: " + result.admitted() + " user(s)");
        Map<String, List<String>> consumedByKind = new LinkedHashMap<>();
        for (Pass pass : result.consumedPasses()) {
            consumedByKind.computeIfAbsent(pass.getKind(), k -> new ArrayList<>()).add(pass.getId());
        }
        for (Map.Entry<String, List<String>> entry : consumedByKind.entrySet()) {
            line.append("; ").append(entry.getKey()).append(" consumed: ").append(String.join(", ", entry.getValue()));
        }
        out.println(line);
    }

    private void printList(Attraction<?> attraction) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("(no users)");
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
        }
    }

    private void printStatus(Attraction<?> attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        out.println("Attraction: " + attraction.getId());
        out.println("Name: " + attraction.getName());
        out.println("Type: " + attraction.getType());
        out.println("Capacity: " + attraction.getCapacity());
        out.println("Users inside: " + attraction.getOccupants().size());
        out.println("Available places: " + attraction.getFreePlaces());
        out.println("Users: " + (emails.isEmpty() ? "(none)" : String.join(", ", emails)));
    }

    private static String money(BigDecimal amount) {
        return InvoicePrinter.money(amount);
    }

    private static void requireFields(String[] parts, String syntax, int... allowed) throws CommandSyntaxException {
        for (int count : allowed) {
            if (parts.length == count) {
                return;
            }
        }
        throw new CommandSyntaxException("wrong number of fields, expected " + syntax);
    }
}
