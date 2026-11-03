package es.upm.etsisi.amusementpark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Parses and executes ';'-separated commands. Interactive input and command
 * files are both processed by {@link #run(BufferedReader)}, so a command
 * behaves the same whatever its source.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";

    private static final String HELP_TEXT = String.join(System.lineSeparator(),
            "Available commands:",
            "  USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>",
            "  USER DELETE;<Email>;<Password>",
            "  USER UPDATE;<Email>;<Password>;<Field>;<NewValue>",
            "      <Field>: NAME, SURNAME, EMAIL, PASSWORD, PHONE, CARD",
            "  SYSTEM DATE;<yyyy-MM-dd>",
            "  SYSTEM PRICE;<Price>",
            "  GROUP CREATE;<Identifier>;<Name>",
            "  GROUP ADD;<Identifier>;<Email>",
            "  GROUP REMOVE;<Identifier>;<Email>",
            "  GROUP DELETE;<Identifier>",
            "  TICKET CREATE;<identifier>;<email>;<date>",
            "  TICKET DELETE;<identifier>",
            "  GROUPTICKET CREATE;<identifier>;<group>;<date>",
            "  GROUPTICKET DELETE;<identifier>",
            "  ATTRACTION CREATE;<identifier>;<name>;<capacity>",
            "  ATTRACTION ENTER;<attraction>;<ticket>",
            "  ATTRACTION EXIT;<attraction>;<email>",
            "  ATTRACTION LIST;<attraction>",
            "  ATTRACTION STATUS;<attractionIdentifier>",
            "  HELP",
            "  EXIT");

    private final ParkController park;
    private final PrintStream out;

    public CommandProcessor(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    /** Processes every line until EXIT or end of input; blank lines are ignored. */
    public void run(BufferedReader input) throws IOException {
        String line;
        boolean running = true;
        while (running && (line = input.readLine()) != null) {
            if (!line.isBlank()) {
                running = execute(line);
            }
        }
    }

    /** Processes the commands stored in a text file, one command per line. */
    public void runFile(Path commandFile) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(commandFile, StandardCharsets.UTF_8)) {
            run(reader);
        }
    }

    /**
     * Executes one command line and prints its result.
     *
     * @return false when the command was EXIT, true otherwise
     */
    public boolean execute(String line) {
        String[] parts = line.split(SEPARATOR, -1);
        String keyword = parts[0].trim();
        try {
            if ("EXIT".equals(keyword)) {
                expect(parts, 1, "EXIT");
                out.println("BYE");
                return false;
            }
            dispatch(keyword, parts);
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private void dispatch(String keyword, String[] p) throws ParkException {
        switch (keyword) {
            case "USER CREATE" -> {
                expect(p, 8, "USER CREATE;<FirstName>;<Surnames>;<DNI>;<Email>;<Password>;<Phone>;<Card>");
                park.createUser(p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
                out.println("USER CREATED");
            }
            case "USER DELETE" -> {
                expect(p, 3, "USER DELETE;<Email>;<Password>");
                park.deleteUser(p[1], p[2]);
                out.println("USER DELETED");
            }
            case "USER UPDATE" -> {
                expect(p, 5, "USER UPDATE;<Email>;<Password>;<Field>;<NewValue>");
                park.updateUser(p[1], p[2], p[3], p[4]);
                out.println("USER UPDATED");
            }
            case "SYSTEM DATE" -> {
                expect(p, 2, "SYSTEM DATE;<yyyy-MM-dd>");
                park.setDate(p[1]);
                out.println("SYSTEM DATE SET TO " + park.getToday());
            }
            case "SYSTEM PRICE" -> {
                expect(p, 2, "SYSTEM PRICE;<Price>");
                park.setPrice(p[1]);
                out.println("SYSTEM PRICE SET TO " + park.getBasePrice().toPlainString() + " EUR");
            }
            case "GROUP CREATE" -> {
                expect(p, 3, "GROUP CREATE;<Identifier>;<Name>");
                park.createGroup(p[1], p[2]);
                out.println("GROUP CREATED");
            }
            case "GROUP ADD" -> {
                expect(p, 3, "GROUP ADD;<Identifier>;<Email>");
                park.addToGroup(p[1], p[2]);
                out.println("USER ADDED TO GROUP");
            }
            case "GROUP REMOVE" -> {
                expect(p, 3, "GROUP REMOVE;<Identifier>;<Email>");
                park.removeFromGroup(p[1], p[2]);
                out.println("USER REMOVED FROM GROUP");
            }
            case "GROUP DELETE" -> {
                expect(p, 2, "GROUP DELETE;<Identifier>");
                park.deleteGroup(p[1]);
                out.println("GROUP DELETED");
            }
            case "TICKET CREATE" -> {
                expect(p, 4, "TICKET CREATE;<identifier>;<email>;<date>");
                Ticket ticket = park.createTicket(p[1], p[2], p[3]);
                out.println("TICKET CREATED. PRICE: " + ticket.getPrice().toPlainString() + " EUR");
            }
            case "TICKET DELETE" -> {
                expect(p, 2, "TICKET DELETE;<identifier>");
                park.deleteTicket(p[1]);
                out.println("TICKET DELETED");
            }
            case "GROUPTICKET CREATE" -> {
                expect(p, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
                Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                out.println("GROUP TICKET CREATED. TOTAL PRICE: " + ticket.getPrice().toPlainString() + " EUR");
            }
            case "GROUPTICKET DELETE" -> {
                expect(p, 2, "GROUPTICKET DELETE;<identifier>");
                park.deleteGroupTicket(p[1]);
                out.println("GROUP TICKET DELETED");
            }
            default -> dispatchAttraction(keyword, p);
        }
    }

    private void dispatchAttraction(String keyword, String[] p) throws ParkException {
        switch (keyword) {
            case "ATTRACTION CREATE" -> {
                expect(p, 4, "ATTRACTION CREATE;<identifier>;<name>;<capacity>");
                park.createAttraction(p[1], p[2], p[3]);
                out.println("ATTRACTION CREATED");
            }
            case "ATTRACTION ENTER" -> {
                expect(p, 3, "ATTRACTION ENTER;<attraction>;<ticket>");
                int admitted = park.enter(p[1], p[2]);
                out.println("ENTERED ATTRACTION: " + admitted + " USER(S)");
            }
            case "ATTRACTION EXIT" -> {
                expect(p, 3, "ATTRACTION EXIT;<attraction>;<email>");
                park.exit(p[1], p[2]);
                out.println("EXITED ATTRACTION");
            }
            case "ATTRACTION LIST" -> {
                expect(p, 2, "ATTRACTION LIST;<attraction>");
                list(park.requireAttraction(p[1]));
            }
            case "ATTRACTION STATUS" -> {
                expect(p, 2, "ATTRACTION STATUS;<attractionIdentifier>");
                status(park.requireAttraction(p[1]));
            }
            case "HELP" -> {
                expect(p, 1, "HELP");
                out.println(HELP_TEXT);
            }
            default -> throw new ParkException("unknown command '" + keyword + "'. Type HELP for the list of commands");
        }
    }

    private void list(Attraction attraction) {
        List<User> occupants = attraction.getOccupants();
        if (occupants.isEmpty()) {
            out.println("NO USERS IN ATTRACTION " + attraction.getIdentifier());
        }
        for (User occupant : occupants) {
            out.println(occupant.getEmail());
        }
    }

    private void status(Attraction attraction) {
        List<User> occupants = attraction.getOccupants();
        StringBuilder emails = new StringBuilder();
        for (User occupant : occupants) {
            emails.append(emails.isEmpty() ? "" : ", ").append(occupant.getEmail());
        }
        out.println("ATTRACTION " + attraction.getIdentifier() + " - " + attraction.getName());
        out.println("  Capacity: " + attraction.getCapacity());
        out.println("  Occupied: " + occupants.size());
        out.println("  Available: " + attraction.getAvailablePlaces());
        out.println("  Users: " + (occupants.isEmpty() ? "none" : emails));
    }

    private static void expect(String[] parts, int expected, String syntax) throws ParkException {
        if (parts.length != expected) {
            throw new ParkException("wrong number of arguments. Usage: " + syntax);
        }
    }
}
