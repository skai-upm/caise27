package es.upm.amusementpark.presentation;

import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.model.Ticket;

/**
 * TICKET and GROUPTICKET commands.
 */
class TicketCommands {

    private static final String DATE_ERROR = "Invalid date. Use yyyy-MM-dd";

    private final TicketController tickets;

    TicketCommands(TicketController tickets) {
        this.tickets = tickets;
    }

    String create(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(3, "TICKET CREATE;<identifier>;<email>;<date>");
        Ticket ticket = tickets.createIndividual(in.get(1), in.get(2), in.date(3, DATE_ERROR));
        return "TICKET CREATED - Price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    String delete(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "TICKET DELETE;<identifier>");
        tickets.deleteIndividual(in.get(1));
        return "TICKET DELETED";
    }

    String createGroup(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(3, "GROUPTICKET CREATE;<identifier>;<group>;<date>");
        Ticket ticket = tickets.createGroupTicket(in.get(1), in.get(2), in.date(3, DATE_ERROR));
        return "GROUPTICKET CREATED - Total price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    String deleteGroup(CommandInput in) throws InvalidCommandException, ParkException {
        in.expect(1, "GROUPTICKET DELETE;<identifier>");
        tickets.deleteGroupTicket(in.get(1));
        return "GROUPTICKET DELETED";
    }
}
