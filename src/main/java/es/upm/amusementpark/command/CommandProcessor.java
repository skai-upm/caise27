package es.upm.amusementpark.command;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.model.UserField;
import es.upm.amusementpark.model.ticket.Ticket;
import es.upm.amusementpark.service.OperationException;
import es.upm.amusementpark.service.Park;
import es.upm.amusementpark.validation.ValueParser;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Interprets a single ';'-separated command line and executes it on the park.
 */
public class CommandProcessor {

    static final List<String> HELP_SYNTAX = List.of(
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "SYSTEM DATE;<yyyy-MM-dd>",
            "SYSTEM PRICE;<Price>",
            "GROUP CREATE;<Identifier>;<Name>",
            "GROUP ADD;<Identifier>;<Email>",
            "GROUP REMOVE;<Identifier>;<Email>",
            "GROUP DELETE;<Identifier>",
            "TICKET CREATE;<identifier>;<email>;<date>",
            "TICKET DELETE;<identifier>",
            "GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "GROUPTICKET DELETE;<identifier>",
            "ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "ATTRACTION ENTER;<attraction>;<ticket>",
            "ATTRACTION EXIT;<attraction>;<email>",
            "ATTRACTION LIST;<attraction>",
            "ATTRACTION STATUS;<attractionIdentifier>",
            "HELP",
            "EXIT");

    static final String HELP_TEXT = "Available commands:" + System.lineSeparator()
            + String.join(System.lineSeparator(), HELP_SYNTAX) + System.lineSeparator()
            + "  <Field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD";

    private final Park park;
    private final PrintStream out;

    public CommandProcessor(Park park, PrintStream out) {
        this.park = park;
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
            switch (p[0]) {
                case "USER CREATE" -> {
                    requireFields(p, 8, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
                    park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    out.println("USER CREATED");
                }
                case "USER DELETE" -> {
                    requireFields(p, 3, "USER DELETE;<Email>;<Password>");
                    park.deleteUser(p[1], p[2]);
                    out.println("USER DELETED");
                }
                case "USER UPDATE" -> {
                    requireFields(p, 5, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>");
                    UserField field = UserField.fromName(p[3]);
                    if (field == null) {
                        throw new OperationException("field cannot be modified: " + p[3]);
                    }
                    park.updateUser(p[1], p[2], field, p[4]);
                    out.println("USER UPDATED");
                }
                case "SYSTEM DATE" -> {
                    requireFields(p, 2, "SYSTEM DATE;<yyyy-MM-dd>");
                    park.setCurrentDate(p[1]);
                    out.println("SYSTEM DATE SET: " + ValueParser.formatDate(park.getCurrentDate()));
                }
                case "SYSTEM PRICE" -> {
                    requireFields(p, 2, "SYSTEM PRICE;<Price>");
                    park.setCurrentPrice(p[1]);
                    out.println("SYSTEM PRICE SET: " + money(park.getCurrentPrice()));
                }
                case "GROUP CREATE" -> {
                    requireFields(p, 3, "GROUP CREATE;<Identifier>;<Name>");
                    park.createGroup(p[1], p[2]);
                    out.println("GROUP CREATED");
                }
                case "GROUP ADD" -> {
                    requireFields(p, 3, "GROUP ADD;<Identifier>;<Email>");
                    park.addToGroup(p[1], p[2]);
                    out.println("GROUP USER ADDED");
                }
                case "GROUP REMOVE" -> {
                    requireFields(p, 3, "GROUP REMOVE;<Identifier>;<Email>");
                    park.removeFromGroup(p[1], p[2]);
                    out.println("GROUP USER REMOVED");
                }
                case "GROUP DELETE" -> {
                    requireFields(p, 2, "GROUP DELETE;<Identifier>");
                    park.deleteGroup(p[1]);
                    out.println("GROUP DELETED");
                }
                case "TICKET CREATE" -> {
                    requireFields(p, 4, "TICKET CREATE;<identifier>;<email>;<date>");
                    Ticket ticket = park.createTicket(p[1], p[2], p[3]);
                    out.println("TICKET CREATED: price " + money(ticket.getPrice()));
                }
                case "TICKET DELETE" -> {
                    requireFields(p, 2, "TICKET DELETE;<identifier>");
                    park.deleteTicket(p[1]);
                    out.println("TICKET DELETED");
                }
                case "GROUPTICKET CREATE" -> {
                    requireFields(p, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
                    Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                    out.println("GROUPTICKET CREATED: total " + money(ticket.getPrice()));
                }
                case "GROUPTICKET DELETE" -> {
                    requireFields(p, 2, "GROUPTICKET DELETE;<identifier>");
                    park.deleteGroupTicket(p[1]);
                    out.println("GROUPTICKET DELETED");
                }
                case "ATTRACTION CREATE" -> {
                    requireFields(p, 4, "ATTRACTION CREATE;<identifier>;<name>;<capacity>");
                    park.createAttraction(p[1], p[2], p[3]);
                    out.println("ATTRACTION CREATED");
                }
                case "ATTRACTION ENTER" -> {
                    requireFields(p, 3, "ATTRACTION ENTER;<attraction>;<ticket>");
                    int entered = park.enterAttraction(p[1], p[2]);
                    out.println("ATTRACTION ENTERED: " + entered + " user(s)");
                }
                case "ATTRACTION EXIT" -> {
                    requireFields(p, 3, "ATTRACTION EXIT;<attraction>;<email>");
                    park.exitAttraction(p[1], p[2]);
                    out.println("ATTRACTION EXITED");
                }
                case "ATTRACTION LIST" -> {
                    requireFields(p, 2, "ATTRACTION LIST;<attraction>");
                    printList(park.findAttraction(p[1]));
                }
                case "ATTRACTION STATUS" -> {
                    requireFields(p, 2, "ATTRACTION STATUS;<attractionIdentifier>");
                    printStatus(park.findAttraction(p[1]));
                }
                case "HELP" -> {
                    requireFields(p, 1, "HELP");
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireFields(p, 1, "EXIT");
                    return false;
                }
                default -> out.println("ERROR: unknown command: " + p[0]);
            }
        } catch (OperationException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void printList(Attraction attraction) {
        if (attraction.getOccupants().isEmpty()) {
            out.println("(no users)");
            return;
        }
        for (User user : attraction.getOccupants()) {
            out.println(user.getEmail());
        }
    }

    private void printStatus(Attraction attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        out.println("Attraction: " + attraction.getId());
        out.println("Name: " + attraction.getName());
        out.println("Capacity: " + attraction.getCapacity());
        out.println("Users inside: " + attraction.getOccupants().size());
        out.println("Available places: " + attraction.getFreePlaces());
        out.println("Users: " + (emails.isEmpty() ? "(none)" : String.join(", ", emails)));
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString() + " EUR";
    }

    private static void requireFields(String[] parts, int expected, String syntax) throws OperationException {
        if (parts.length != expected) {
            throw new OperationException("wrong number of fields, expected " + syntax);
        }
    }
}
