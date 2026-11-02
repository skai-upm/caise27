package es.upm.etsisi.park;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Parses and executes text commands. Interactive and file input both go through
 * {@link #run(BufferedReader, PrintStream)}, so a command behaves the same whatever its source.
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

    private static final String SEPARATOR = ";";

    private final Park park;
    private boolean exitRequested;

    public CommandProcessor(Park park) {
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
            output.println(execute(line));
        }
    }

    /**
     * Processes the commands stored in a text file, one per line.
     *
     * @throws IOException if the file does not exist, is not a regular file or cannot be read
     */
    public void runFile(Path commands, PrintStream output) throws IOException {
        if (!Files.isRegularFile(commands) || !Files.isReadable(commands)) {
            throw new IOException("Cannot read command file: " + commands);
        }
        try (BufferedReader reader = Files.newBufferedReader(commands, StandardCharsets.UTF_8)) {
            run(reader, output);
        }
    }

    /**
     * Executes one command and returns the text to show. Never throws for bad input.
     */
    public String execute(String line) {
        String[] p = line.trim().split(SEPARATOR, -1);
        try {
            return switch (p[0]) {
                case "USER CREATE" -> userCreate(p);
                case "USER DELETE" -> userDelete(p);
                case "USER UPDATE" -> userUpdate(p);
                case "SYSTEM DATE" -> systemDate(p);
                case "SYSTEM PRICE" -> systemPrice(p);
                case "GROUP CREATE" -> groupCreate(p);
                case "GROUP ADD" -> groupAdd(p);
                case "GROUP REMOVE" -> groupRemove(p);
                case "GROUP DELETE" -> groupDelete(p);
                case "TICKET CREATE" -> ticketCreate(p);
                case "TICKET DELETE" -> ticketDelete(p);
                case "GROUPTICKET CREATE" -> groupTicketCreate(p);
                case "GROUPTICKET DELETE" -> groupTicketDelete(p);
                case "ATTRACTION CREATE" -> attractionCreate(p);
                case "ATTRACTION ENTER" -> attractionEnter(p);
                case "ATTRACTION EXIT" -> attractionExit(p);
                case "ATTRACTION LIST" -> attractionList(p);
                case "ATTRACTION STATUS" -> attractionStatus(p);
                case "HELP" -> help(p);
                case "EXIT" -> exit(p);
                default -> "ERROR: Unknown command: " + p[0];
            };
        } catch (ParkException e) {
            return "ERROR: " + e.getMessage();
        } catch (RuntimeException e) {
            return "ERROR: Unexpected failure: " + e.getMessage();
        }
    }

    private String userCreate(String[] p) {
        expectFields(p, 8);
        park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
        return "USER CREATED";
    }

    private String userDelete(String[] p) {
        expectFields(p, 3);
        park.deleteUser(p[1], p[2]);
        return "USER DELETED";
    }

    private String userUpdate(String[] p) {
        expectFields(p, 5);
        park.updateUser(p[1], p[2], p[3], p[4]);
        return "USER UPDATED";
    }

    private String systemDate(String[] p) {
        expectFields(p, 2);
        park.setToday(p[1]);
        return "SYSTEM DATE SET TO " + ValueParser.formatDate(park.getToday());
    }

    private String systemPrice(String[] p) {
        expectFields(p, 2);
        park.setBasePrice(p[1]);
        return "SYSTEM PRICE SET TO " + Money.format(park.getBasePrice());
    }

    private String groupCreate(String[] p) {
        expectFields(p, 3);
        park.createGroup(p[1], p[2]);
        return "GROUP CREATED";
    }

    private String groupAdd(String[] p) {
        expectFields(p, 3);
        park.addToGroup(p[1], p[2]);
        return "GROUP USER ADDED";
    }

    private String groupRemove(String[] p) {
        expectFields(p, 3);
        park.removeFromGroup(p[1], p[2]);
        return "GROUP USER REMOVED";
    }

    private String groupDelete(String[] p) {
        expectFields(p, 2);
        park.deleteGroup(p[1]);
        return "GROUP DELETED";
    }

    private String ticketCreate(String[] p) {
        expectFields(p, 4);
        IndividualTicket t = park.createTicket(p[1], p[2], p[3]);
        return "TICKET CREATED " + t.getId() + " PRICE " + Money.format(t.getPrice());
    }

    private String ticketDelete(String[] p) {
        expectFields(p, 2);
        park.deleteTicket(p[1]);
        return "TICKET DELETED";
    }

    private String groupTicketCreate(String[] p) {
        expectFields(p, 4);
        GroupTicket t = park.createGroupTicket(p[1], p[2], p[3]);
        return "GROUPTICKET CREATED " + t.getId() + " MEMBERS " + t.getComposition().size()
                + " DISCOUNT " + t.getComposition().get(0).getDiscountPercent() + "% TOTAL " + Money.format(t.getPrice());
    }

    private String groupTicketDelete(String[] p) {
        expectFields(p, 2);
        park.deleteGroupTicket(p[1]);
        return "GROUPTICKET DELETED";
    }

    private String attractionCreate(String[] p) {
        expectFields(p, 4);
        park.createAttraction(p[1], p[2], p[3]);
        return "ATTRACTION CREATED";
    }

    private String attractionEnter(String[] p) {
        expectFields(p, 3);
        int entered = park.enter(p[1], p[2]);
        return "ATTRACTION ENTERED " + entered + " USER(S)";
    }

    private String attractionExit(String[] p) {
        expectFields(p, 3);
        park.exit(p[1], p[2]);
        return "ATTRACTION EXITED";
    }

    private String attractionList(String[] p) {
        expectFields(p, 2);
        List<User> occupants = park.requireAttraction(p[1]).getOccupants();
        if (occupants.isEmpty()) {
            return "(empty)";
        }
        return String.join(System.lineSeparator(), emails(occupants));
    }

    private String attractionStatus(String[] p) {
        expectFields(p, 2);
        Attraction a = park.requireAttraction(p[1]);
        List<String> inside = emails(a.getOccupants());
        return String.join(System.lineSeparator(),
                "ATTRACTION " + a.getId() + " - " + a.getName(),
                "Capacity: " + a.getCapacity(),
                "Inside: " + inside.size(),
                "Available: " + a.getAvailable(),
                "Users: " + (inside.isEmpty() ? "(none)" : String.join(", ", inside)));
    }

    private static List<String> emails(List<User> users) {
        return users.stream().map(User::getEmail).toList();
    }

    private String help(String[] p) {
        expectFields(p, 1);
        return HELP_TEXT;
    }

    private String exit(String[] p) {
        expectFields(p, 1);
        exitRequested = true;
        return "BYE";
    }

    private static void expectFields(String[] parts, int expected) {
        if (parts.length != expected) {
            throw new ParkException("Malformed command: expected " + (expected - 1)
                    + " parameter(s) but got " + (parts.length - 1));
        }
    }
}
