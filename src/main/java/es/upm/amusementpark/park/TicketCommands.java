package es.upm.amusementpark.park;

import es.upm.amusementpark.cli.CommandProcessor;
import java.io.PrintStream;
import java.time.LocalDate;

/**
 * {@code TICKET CREATE} and {@code TICKET DELETE} commands.
 */
public final class TicketCommands {

    private final ParkController park;
    private final PrintStream out;

    private TicketCommands(ParkController park, PrintStream out) {
        this.park = park;
        this.out = out;
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        TicketCommands commands = new TicketCommands(park, out);
        processor.register("TICKET CREATE", commands::create);
        processor.register("TICKET DELETE", commands::delete);
        processor.register("GROUPTICKET CREATE", commands::createGroupTicket);
        processor.register("GROUPTICKET DELETE", commands::deleteGroupTicket);
    }

    private boolean create(String line) {
        String[] f = fields(line);
        if (f.length != 4) {
            out.println("ERROR: Invalid syntax. Usage: TICKET CREATE;<identifier>;<email>;<date>");
            return true;
        }
        LocalDate date = SystemCommands.parseDate(f[3]);
        if (date == null) {
            out.println("ERROR: Invalid date. Expected yyyy-MM-dd");
            return true;
        }
        try {
            Ticket ticket = park.createIndividualTicket(f[1], f[2], date);
            out.println("TICKET CREATED. PRICE: " + ticket.getPrice().toPlainString());
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean delete(String line) {
        String[] f = fields(line);
        if (f.length != 2) {
            out.println("ERROR: Invalid syntax. Usage: TICKET DELETE;<identifier>");
            return true;
        }
        try {
            park.deleteTicket(f[1]);
            out.println("TICKET DELETED");
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean createGroupTicket(String line) {
        String[] f = fields(line);
        if (f.length != 4) {
            out.println("ERROR: Invalid syntax. Usage: GROUPTICKET CREATE;<identifier>;<group>;<date>");
            return true;
        }
        LocalDate date = SystemCommands.parseDate(f[3]);
        if (date == null) {
            out.println("ERROR: Invalid date. Expected yyyy-MM-dd");
            return true;
        }
        try {
            GroupTicket ticket = park.createGroupTicket(f[1], f[2], date);
            out.println("GROUP TICKET CREATED. TOTAL PRICE: " + ticket.getPrice().toPlainString());
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    private boolean deleteGroupTicket(String line) {
        String[] f = fields(line);
        if (f.length != 2) {
            out.println("ERROR: Invalid syntax. Usage: GROUPTICKET DELETE;<identifier>");
            return true;
        }
        try {
            park.deleteGroupTicket(f[1]);
            out.println("GROUP TICKET DELETED");
        } catch (ParkException e) {
            out.println("ERROR: " + e.getMessage());
        }
        return true;
    }

    static String[] fields(String line) {
        String[] f = line.split(";", -1);
        for (int i = 0; i < f.length; i++) {
            f[i] = f[i].trim();
        }
        return f;
    }
}
