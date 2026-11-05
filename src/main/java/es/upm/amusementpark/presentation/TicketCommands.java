package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.GroupTicket;
import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.Ticket;
import java.io.PrintStream;

/**
 * {@code TICKET CREATE/DELETE} and {@code GROUPTICKET CREATE/DELETE} commands.
 */
public final class TicketCommands {

    private TicketCommands() {
    }

    public static void register(CommandProcessor processor, ParkController park, PrintStream out) {
        processor.register("TICKET CREATE", line -> {
            String[] f = CommandSupport.fields(line, 4, "TICKET CREATE;<identifier>;<email>;<date>");
            Ticket ticket = park.createIndividualTicket(f[1], f[2], CommandSupport.ticketDate(f[3]));
            out.println("TICKET CREATED. PRICE: " + ticket.getPrice().toPlainString());
            return true;
        });
        processor.register("TICKET DELETE", line -> {
            String[] f = CommandSupport.fields(line, 2, "TICKET DELETE;<identifier>");
            park.deleteTicket(f[1]);
            out.println("TICKET DELETED");
            return true;
        });
        processor.register("GROUPTICKET CREATE", line -> {
            String[] f = CommandSupport.fields(line, 4, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
            GroupTicket ticket = park.createGroupTicket(f[1], f[2], CommandSupport.ticketDate(f[3]));
            out.println("GROUP TICKET CREATED. TOTAL PRICE: " + ticket.getPrice().toPlainString());
            return true;
        });
        processor.register("GROUPTICKET DELETE", line -> {
            String[] f = CommandSupport.fields(line, 2, "GROUPTICKET DELETE;<identifier>");
            park.deleteGroupTicket(f[1]);
            out.println("GROUP TICKET DELETED");
            return true;
        });
    }
}
