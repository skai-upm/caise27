package es.upm.parque.presentation;

import es.upm.parque.business.ParkController;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.TicketManager;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;

/**
 * Parses the TICKET and GROUPTICKET commands and formats their responses; the rules live in {@link TicketManager}.
 */
class TicketCommands {

    static final String TICKET_CREATE = "TICKET CREATE";
    static final String TICKET_DELETE = "TICKET DELETE";
    static final String GROUPTICKET_CREATE = "GROUPTICKET CREATE";
    static final String GROUPTICKET_DELETE = "GROUPTICKET DELETE";

    private final TicketManager tickets;

    TicketCommands(ParkController park) {
        this.tickets = new TicketManager(park);
    }

    String create(String[] e) throws InvalidCommandException, ParkException {
        if (e.length != 4 || e[1].isEmpty()) {
            throw new InvalidCommandException("TICKET CREATE expects: TICKET CREATE;<id>;<email>;<yyyy-MM-dd>");
        }
        IndividualTicket ticket = tickets.createIndividual(e[1], e[2], e[3]);
        return "OK: ticket " + ticket.getId() + " created for " + ticket.getUser().getEmail() + " on "
                + ticket.getDate() + ", price " + ticket.getPrice().toPlainString() + " EUR";
    }

    String delete(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 2, "TICKET DELETE expects: TICKET DELETE;<id>");
        return "OK: ticket " + tickets.deleteIndividual(e[1]).getId() + " deleted";
    }

    String createGroupTicket(String[] e) throws InvalidCommandException, ParkException {
        if (e.length != 4 || e[1].isEmpty()) {
            throw new InvalidCommandException("GROUPTICKET CREATE expects: GROUPTICKET CREATE;<id>;<group>;<yyyy-MM-dd>");
        }
        GroupTicket ticket = tickets.createGroupTicket(e[1], e[2], e[3]);
        int members = ticket.getComposition().size();
        return "OK: group ticket " + ticket.getId() + " created for group " + e[2] + " on " + ticket.getDate() + " ("
                + members + " tickets, discount " + GroupTicket.discountPercentFor(members) + "%), total "
                + ticket.getPrice().toPlainString() + " EUR";
    }

    String deleteGroupTicket(String[] e) throws InvalidCommandException, ParkException {
        CommandProcessor.expect(e, 2, "GROUPTICKET DELETE expects: GROUPTICKET DELETE;<id>");
        GroupTicket ticket = tickets.deleteGroupTicket(e[1]);
        return "OK: group ticket " + ticket.getId() + " deleted with its " + ticket.getComposition().size() + " tickets";
    }
}
