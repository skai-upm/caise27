package es.upm.parque.command;

import java.time.LocalDate;

import es.upm.parque.model.IndividualTicket;
import es.upm.parque.model.Ticket;
import es.upm.parque.model.User;
import es.upm.parque.park.ParkController;
import es.upm.parque.park.SystemValues;

/**
 * Executes the individual TICKET commands against the central park management.
 */
class TicketCommands {

    static final String TICKET_CREATE = "TICKET CREATE";
    static final String TICKET_DELETE = "TICKET DELETE";

    private final ParkController park;

    TicketCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] e) {
        if (e.length != 4 || e[1].isEmpty()) {
            return error("TICKET CREATE expects: TICKET CREATE;<id>;<email>;<yyyy-MM-dd>");
        }
        String id = e[1];
        if (park.getTickets().exists(id)) {
            return error("a ticket with identifier " + id + " already exists");
        }
        User user = park.getUsers().findByEmail(e[2]);
        if (user == null) {
            return error("no user with email " + e[2]);
        }
        LocalDate date = SystemValues.parseDate(e[3]);
        if (date == null) {
            return error("invalid date '" + e[3] + "' (expected yyyy-MM-dd)");
        }
        if (date.isBefore(park.getCurrentDate())) {
            return error("tickets cannot be created for a date before " + park.getCurrentDate());
        }
        IndividualTicket ticket = new IndividualTicket(id, user, date, park.getBasePrice());
        park.getTickets().add(ticket);
        return "OK: ticket " + id + " created for " + user.getEmail() + " on " + date
                + ", price " + ticket.getPrice().toPlainString() + " EUR";
    }

    String delete(String[] e) {
        if (e.length != 2) {
            return error("TICKET DELETE expects: TICKET DELETE;<id>");
        }
        Ticket ticket = park.getTickets().findById(e[1]);
        if (!(ticket instanceof IndividualTicket)) {
            return error("no individual ticket with identifier " + e[1]);
        }
        if (park.isPartOfGroupTicket(ticket)) {
            return error("ticket " + e[1] + " belongs to a group ticket and cannot be deleted on its own");
        }
        if (!ticket.isDeletableOn(park.getCurrentDate())) {
            return error("ticket " + e[1] + " is for today or an earlier date and is kept as a historical record");
        }
        park.getTickets().remove(e[1]);
        return "OK: ticket " + e[1] + " deleted";
    }

    private static String error(String message) {
        return "ERROR: " + message;
    }
}
