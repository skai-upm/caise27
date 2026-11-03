package es.upm.etsisi.amusementpark.presentation;

import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.controller.InvoiceController;
import es.upm.etsisi.amusementpark.business.controller.ParkController;
import es.upm.etsisi.amusementpark.business.invoice.Invoice;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Parses and executes ';'-separated commands. Interactive input and command
 * files are both processed by {@link #run(BufferedReader)}, so a command
 * behaves the same whatever its source. Every rejected operation arrives as a
 * {@link ParkException} and is reported without stopping processing.
 */
public class CommandProcessor {

    private static final String SEPARATOR = ";";
    private static final String EUR = " EUR";

    private final ParkController park;
    private final InvoiceController invoices;
    private final Map<String, HelpGenerator> helpGenerators;
    private final PrintStream out;

    /**
     * @param helpGenerators help generator for each help command keyword, e.g. HELP and HELP EXTENDED
     */
    public CommandProcessor(ParkController park, InvoiceController invoices, Map<String, HelpGenerator> helpGenerators,
                            PrintStream out) {
        this.park = park;
        this.invoices = invoices;
        this.helpGenerators = Map.copyOf(helpGenerators);
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
        HelpGenerator help = helpGenerators.get(keyword);
        if (help != null) {
            expect(p, 1, keyword);
            out.println(help.generate());
        } else if (keyword.startsWith("USER ") || keyword.startsWith("SYSTEM ")) {
            userOrSystem(keyword, p);
        } else if (keyword.startsWith("GROUP ") || keyword.startsWith("TICKET ") || keyword.startsWith("GROUPTICKET ")) {
            groupOrTicket(keyword, p);
        } else if (keyword.startsWith("ATTRACTION ")) {
            attraction(keyword, p);
        } else {
            passOrInvoice(keyword, p);
        }
    }

    private void userOrSystem(String keyword, String[] p) throws ParkException {
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
                out.println("SYSTEM PRICE SET TO " + money(park.getBasePrice()));
            }
            default -> throw unknown(keyword);
        }
    }

    private void groupOrTicket(String keyword, String[] p) throws ParkException {
        switch (keyword) {
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
                out.println("TICKET CREATED. PRICE: " + money(ticket.getPrice()));
            }
            case "TICKET DELETE" -> {
                expect(p, 2, "TICKET DELETE;<identifier>");
                park.deleteTicket(p[1]);
                out.println("TICKET DELETED");
            }
            case "GROUPTICKET CREATE" -> {
                expect(p, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
                Ticket ticket = park.createGroupTicket(p[1], p[2], p[3]);
                out.println("GROUP TICKET CREATED. TOTAL PRICE: " + money(ticket.getPrice()));
            }
            case "GROUPTICKET DELETE" -> {
                expect(p, 2, "GROUPTICKET DELETE;<identifier>");
                park.deleteGroupTicket(p[1]);
                out.println("GROUP TICKET DELETED");
            }
            default -> throw unknown(keyword);
        }
    }

    private void attraction(String keyword, String[] p) throws ParkException {
        switch (keyword) {
            case "ATTRACTION CREATE" -> {
                expectBetween(p, 4, 5, "ATTRACTION CREATE;<identifier>;<name>;<capacity>[;<type>]");
                Attraction created = park.createAttraction(p[1], p[2], p[3], p.length == 5 ? p[4] : null);
                out.println("ATTRACTION CREATED (" + created.getType() + ")");
            }
            case "ATTRACTION ENTER" -> {
                expectBetween(p, 3, 4, "ATTRACTION ENTER;<attraction>;<ticket>[;SPEED]");
                printEntry(park.enter(p[1], p[2], p.length == 4 ? p[3] : null));
            }
            case "ATTRACTION EXIT" -> {
                expect(p, 3, "ATTRACTION EXIT;<attraction>;<email>");
                park.exit(p[1], p[2]);
                out.println("EXITED ATTRACTION");
            }
            case "ATTRACTION LIST" -> {
                expect(p, 2, "ATTRACTION LIST;<attraction>");
                printList(park.requireAttraction(p[1]));
            }
            case "ATTRACTION STATUS" -> {
                expect(p, 2, "ATTRACTION STATUS;<attractionIdentifier>");
                printStatus(park.requireAttraction(p[1]));
            }
            default -> throw unknown(keyword);
        }
    }

    private void passOrInvoice(String keyword, String[] p) throws ParkException {
        switch (keyword) {
            case "SPEEDPASS CREATE" -> {
                expect(p, 4, "SPEEDPASS CREATE;<purchaseIdentifier>;<individualTicket>;<quantity>");
                List<SpeedPass> created = park.createSpeedPasses(p[1], p[2], p[3]);
                BigDecimal total = BigDecimal.ZERO.setScale(2);
                for (SpeedPass pass : created) {
                    total = total.add(pass.getPrice());
                }
                out.println("SPEEDPASS CREATED: " + created.get(0).getIdentifier()
                        + (created.size() > 1 ? " to " + created.get(created.size() - 1).getIdentifier() : "")
                        + ". TOTAL PRICE: " + money(total));
            }
            case "SPEEDPASS DELETE" -> {
                expect(p, 2, "SPEEDPASS DELETE;<speedPassIdentifier>");
                park.deletePass(p[1], SpeedPass.class);
                out.println("SPEEDPASS DELETED");
            }
            case "SPECIALPASS CREATE" -> {
                expect(p, 3, "SPECIALPASS CREATE;<identifier>;<individualTicket>");
                SpecialPass pass = park.createSpecialPass(p[1], p[2]);
                out.println("SPECIAL PASS CREATED. PRICE: " + money(pass.getPrice()));
            }
            case "SPECIALPASS DELETE" -> {
                expect(p, 2, "SPECIALPASS DELETE;<specialPassIdentifier>");
                park.deletePass(p[1], SpecialPass.class);
                out.println("SPECIAL PASS DELETED");
            }
            case "INVOICE CREATE" -> {
                expect(p, 3, "INVOICE CREATE;USER;<email> or INVOICE CREATE;GROUP;<groupIdentifier>");
                printInvoice(invoices.create(p[1], p[2]));
            }
            default -> throw unknown(keyword);
        }
    }

    private void printEntry(EntryResult result) {
        out.println("ENTERED ATTRACTION: " + result.admitted().size() + " USER(S)");
        for (Pass pass : result.consumedPasses()) {
            String kind = pass instanceof SpecialPass ? "SPECIAL PASS" : "SPEEDPASS";
            out.println(kind + " CONSUMED: " + pass.getIdentifier());
        }
    }

    private void printList(Attraction attraction) {
        List<User> occupants = attraction.getOccupants();
        if (occupants.isEmpty()) {
            out.println("NO USERS IN ATTRACTION " + attraction.getIdentifier());
        }
        for (User occupant : occupants) {
            out.println(occupant.getEmail());
        }
    }

    private void printStatus(Attraction attraction) {
        List<User> occupants = attraction.getOccupants();
        StringBuilder emails = new StringBuilder();
        for (User occupant : occupants) {
            emails.append(emails.isEmpty() ? "" : ", ").append(occupant.getEmail());
        }
        out.println("ATTRACTION " + attraction.getIdentifier() + " - " + attraction.getName());
        out.println("  Type: " + attraction.getType());
        out.println("  Capacity: " + attraction.getCapacity());
        out.println("  Occupied: " + occupants.size());
        out.println("  Available: " + attraction.getAvailablePlaces());
        out.println("  Users: " + (occupants.isEmpty() ? "none" : emails));
    }

    private void printInvoice(Invoice invoice) {
        out.println(invoice.getTitle());
        invoice.getHeader().forEach(line -> out.println("  " + line));
        for (Invoice.Section section : invoice.getSections()) {
            out.println(section.title() + ":");
            for (Invoice.Item item : section.items()) {
                out.println("  " + item.description() + ": " + money(item.amount()));
                item.details().forEach(detail -> out.println("      " + detail));
            }
        }
        out.println("TOTAL: " + money(invoice.getTotal()));
    }

    private static String money(BigDecimal amount) {
        return amount.toPlainString() + EUR;
    }

    private static ParkException unknown(String keyword) {
        return new InvalidDataException("unknown command '" + keyword + "'. Type HELP for the list of commands");
    }

    private static void expect(String[] parts, int expected, String syntax) throws InvalidDataException {
        expectBetween(parts, expected, expected, syntax);
    }

    private static void expectBetween(String[] parts, int min, int max, String syntax) throws InvalidDataException {
        if (parts.length < min || parts.length > max) {
            throw new InvalidDataException("wrong number of arguments. Usage: " + syntax);
        }
    }
}
