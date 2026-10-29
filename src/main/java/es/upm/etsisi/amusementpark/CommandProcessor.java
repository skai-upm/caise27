package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Interprets text commands. The same instance and {@link #run} loop serve interactive and file input.
 */
public class CommandProcessor {

    static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "USER DELETE;<Email>;<Password>",
            "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "    <Field>: NAME | SURNAME | EMAIL | PASSWORD | PHONE | CARD",
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
    private boolean exitRequested;

    public CommandProcessor(ParkController park) {
        this.park = park;
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    /**
     * Processes commands line by line until EXIT or end of input. Blank lines are ignored.
     */
    public void run(BufferedReader input, PrintStream output) throws IOException {
        String line;
        while (!exitRequested && (line = input.readLine()) != null) {
            if (line.isBlank()) {
                continue;
            }
            output.println(process(line));
        }
    }

    /**
     * Executes one command line and returns the message to show. Never throws for bad input.
     */
    public String process(String line) {
        String[] p = line.strip().split(";", -1);
        String command = p[0].strip();
        try {
            return switch (command) {
                case "USER CREATE" -> {
                    requireArgs(p, 8, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
                    park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                    yield "USER CREATED";
                }
                case "USER DELETE" -> {
                    requireArgs(p, 3, "USER DELETE;<Email>;<Password>");
                    park.deleteUser(p[1], p[2]);
                    yield "USER DELETED";
                }
                case "USER UPDATE" -> {
                    requireArgs(p, 5, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>");
                    park.updateUser(p[1], p[2], p[3], p[4]);
                    yield "USER UPDATED";
                }
                case "SYSTEM DATE" -> {
                    requireArgs(p, 2, "SYSTEM DATE;<yyyy-MM-dd>");
                    park.setCurrentDate(p[1]);
                    yield "SYSTEM DATE SET: " + park.getCurrentDate();
                }
                case "SYSTEM PRICE" -> {
                    requireArgs(p, 2, "SYSTEM PRICE;<Price>");
                    park.setCurrentPrice(p[1]);
                    yield "SYSTEM PRICE SET: " + park.getCurrentPrice().toPlainString() + " EUR";
                }
                case "GROUP CREATE" -> {
                    requireArgs(p, 3, "GROUP CREATE;<Identifier>;<Name>");
                    park.createGroup(p[1], p[2]);
                    yield "GROUP CREATED";
                }
                case "GROUP ADD" -> {
                    requireArgs(p, 3, "GROUP ADD;<Identifier>;<Email>");
                    park.addToGroup(p[1], p[2]);
                    yield "USER ADDED TO GROUP";
                }
                case "GROUP REMOVE" -> {
                    requireArgs(p, 3, "GROUP REMOVE;<Identifier>;<Email>");
                    park.removeFromGroup(p[1], p[2]);
                    yield "USER REMOVED FROM GROUP";
                }
                case "GROUP DELETE" -> {
                    requireArgs(p, 2, "GROUP DELETE;<Identifier>");
                    park.deleteGroup(p[1]);
                    yield "GROUP DELETED";
                }
                case "TICKET CREATE" -> {
                    requireArgs(p, 4, "TICKET CREATE;<identifier>;<email>;<date>");
                    Ticket ticket = park.createTicket(p[1], p[2], p[3]);
                    yield "TICKET CREATED. PRICE: " + ticket.getPrice().toPlainString() + " EUR";
                }
                case "TICKET DELETE" -> {
                    requireArgs(p, 2, "TICKET DELETE;<identifier>");
                    park.deleteTicket(p[1]);
                    yield "TICKET DELETED";
                }
                case "GROUPTICKET CREATE" -> {
                    requireArgs(p, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
                    Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                    yield "GROUPTICKET CREATED. TOTAL PRICE: " + ticket.getPrice().toPlainString() + " EUR";
                }
                case "GROUPTICKET DELETE" -> {
                    requireArgs(p, 2, "GROUPTICKET DELETE;<identifier>");
                    park.deleteGroupTicket(p[1]);
                    yield "GROUPTICKET DELETED";
                }
                case "ATTRACTION CREATE" -> {
                    requireArgs(p, 4, "ATTRACTION CREATE;<identifier>;<name>;<capacity>");
                    park.createAttraction(p[1], p[2], p[3]);
                    yield "ATTRACTION CREATED";
                }
                case "ATTRACTION ENTER" -> {
                    requireArgs(p, 3, "ATTRACTION ENTER;<attraction>;<ticket>");
                    int entered = park.enterAttraction(p[1], p[2]);
                    yield "ATTRACTION ENTERED: " + entered + " user(s)";
                }
                case "ATTRACTION EXIT" -> {
                    requireArgs(p, 3, "ATTRACTION EXIT;<attraction>;<email>");
                    park.exitAttraction(p[1], p[2]);
                    yield "ATTRACTION EXITED";
                }
                case "ATTRACTION LIST" -> {
                    requireArgs(p, 2, "ATTRACTION LIST;<attraction>");
                    yield listOccupants(park.requireAttraction(p[1]));
                }
                case "ATTRACTION STATUS" -> {
                    requireArgs(p, 2, "ATTRACTION STATUS;<attractionIdentifier>");
                    yield status(park.requireAttraction(p[1]));
                }
                case "HELP" -> {
                    requireArgs(p, 1, "HELP");
                    yield HELP_TEXT;
                }
                case "EXIT" -> {
                    requireArgs(p, 1, "EXIT");
                    exitRequested = true;
                    yield "BYE";
                }
                default -> "ERROR: unknown command: " + command;
            };
        } catch (OperationException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: command could not be processed: " + e.getMessage();
        }
    }

    private static String listOccupants(Attraction attraction) {
        List<String> emails = emails(attraction);
        if (emails.isEmpty()) {
            return "NO USERS IN ATTRACTION " + attraction.getId();
        }
        return String.join(System.lineSeparator(), emails);
    }

    private static String status(Attraction a) {
        List<String> emails = emails(a);
        return String.join(System.lineSeparator(),
                "ATTRACTION: " + a.getId(),
                "NAME: " + a.getName(),
                "CAPACITY: " + a.getCapacity(),
                "INSIDE: " + emails.size(),
                "AVAILABLE: " + a.getAvailablePlaces(),
                "USERS: " + (emails.isEmpty() ? "-" : String.join(", ", emails)));
    }

    private static List<String> emails(Attraction attraction) {
        List<String> emails = new ArrayList<>();
        for (User user : attraction.getOccupants()) {
            emails.add(user.getEmail());
        }
        return emails;
    }

    private static void requireArgs(String[] parts, int expected, String syntax) throws OperationException {
        if (parts.length != expected) {
            throw new OperationException("wrong number of arguments, expected " + syntax);
        }
    }
}
