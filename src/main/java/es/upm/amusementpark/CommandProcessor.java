package es.upm.amusementpark;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interprets a single ';'-separated command line and prints its result.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "  <Field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
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

    private final ParkController park;
    private final PrintStream out;

    public CommandProcessor(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    /**
     * Processes one command line.
     *
     * @return {@code false} if the application must stop (EXIT), {@code true} otherwise
     */
    public boolean process(String line) {
        String[] p = line.split(";", -1);
        String command = p[0].trim();
        try {
            switch (command) {
                case "USER CREATE" -> {
                    requireArgs(p, 7);
                    park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    out.println("USER CREATED");
                }
                case "USER DELETE" -> {
                    requireArgs(p, 2);
                    park.deleteUser(p[1], p[2]);
                    out.println("USER DELETED");
                }
                case "USER UPDATE" -> {
                    requireArgs(p, 4);
                    park.updateUser(p[1], p[2], p[3], p[4]);
                    out.println("USER UPDATED");
                }
                case "SYSTEM DATE" -> {
                    requireArgs(p, 1);
                    park.setCurrentDate(ValueParser.parseDate(p[1]));
                    out.println("SYSTEM DATE SET;" + park.getCurrentDate());
                }
                case "SYSTEM PRICE" -> {
                    requireArgs(p, 1);
                    park.setPrice(ValueParser.parsePrice(p[1]));
                    out.println("SYSTEM PRICE SET;" + money(park.getPrice()));
                }
                case "GROUP CREATE" -> {
                    requireArgs(p, 2);
                    park.createGroup(p[1], p[2]);
                    out.println("GROUP CREATED");
                }
                case "GROUP ADD" -> {
                    requireArgs(p, 2);
                    park.addToGroup(p[1], p[2]);
                    out.println("GROUP USER ADDED");
                }
                case "GROUP REMOVE" -> {
                    requireArgs(p, 2);
                    park.removeFromGroup(p[1], p[2]);
                    out.println("GROUP USER REMOVED");
                }
                case "GROUP DELETE" -> {
                    requireArgs(p, 1);
                    park.deleteGroup(p[1]);
                    out.println("GROUP DELETED");
                }
                case "TICKET CREATE" -> {
                    requireArgs(p, 3);
                    Ticket ticket = park.createTicket(p[1], p[2], p[3]);
                    out.println("TICKET CREATED;PRICE=" + money(ticket.getPrice()));
                }
                case "TICKET DELETE" -> {
                    requireArgs(p, 1);
                    park.deleteTicket(p[1]);
                    out.println("TICKET DELETED");
                }
                case "GROUPTICKET CREATE" -> {
                    requireArgs(p, 3);
                    Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                    out.println("GROUPTICKET CREATED;TOTAL=" + money(ticket.getPrice()));
                }
                case "GROUPTICKET DELETE" -> {
                    requireArgs(p, 1);
                    park.deleteGroupTicket(p[1]);
                    out.println("GROUPTICKET DELETED");
                }
                case "ATTRACTION CREATE" -> {
                    requireArgs(p, 3);
                    park.createAttraction(p[1], p[2], p[3]);
                    out.println("ATTRACTION CREATED");
                }
                case "ATTRACTION ENTER" -> {
                    requireArgs(p, 2);
                    List<User> entered = park.enterAttraction(p[1], p[2]);
                    out.println("ATTRACTION ENTERED;" + entered.size());
                }
                case "ATTRACTION EXIT" -> {
                    requireArgs(p, 2);
                    park.exitAttraction(p[1], p[2]);
                    out.println("ATTRACTION EXITED");
                }
                case "ATTRACTION LIST" -> {
                    requireArgs(p, 1);
                    printOccupants(park.requireAttraction(p[1]));
                }
                case "ATTRACTION STATUS" -> {
                    requireArgs(p, 1);
                    printStatus(park.requireAttraction(p[1]));
                }
                case "HELP" -> {
                    requireArgs(p, 0);
                    out.println(HELP_TEXT);
                }
                case "EXIT" -> {
                    requireArgs(p, 0);
                    out.println("EXIT");
                    return false;
                }
                default -> out.println("ERROR: unknown command: " + command);
            }
        } catch (IllegalArgumentException e) {
            out.println("ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            out.println("ERROR: could not process command: " + e.getMessage());
        }
        return true;
    }

    private void printOccupants(Attraction attraction) {
        List<User> occupants = attraction.getOccupants();
        if (occupants.isEmpty()) {
            out.println("NO USERS");
            return;
        }
        for (User user : occupants) {
            out.println(user.getEmail());
        }
    }

    private void printStatus(Attraction a) {
        out.println("ATTRACTION;" + a.getId());
        out.println("NAME;" + a.getName());
        out.println("CAPACITY;" + a.getCapacity());
        out.println("INSIDE;" + a.getOccupants().size());
        out.println("AVAILABLE;" + a.getAvailable());
        out.print("USERS;");
        List<String> emails = a.getOccupants().stream().map(User::getEmail).toList();
        out.println(String.join(",", emails));
    }

    private static String money(BigDecimal amount) {
        return amount.setScale(2).toPlainString();
    }

    private static void requireArgs(String[] parts, int expected) {
        if (parts.length - 1 != expected) {
            throw new IllegalArgumentException("expected " + expected + " arguments but got "
                    + (parts.length - 1) + " (type HELP for syntax)");
        }
    }
}
