package es.upm.amusementpark.command;

import java.time.LocalDate;

import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.ParkController;

/**
 * TICKET CREATE / DELETE commands.
 */
class TicketCommands {

    private final ParkController park;

    TicketCommands(ParkController park) {
        this.park = park;
    }

    String create(String[] parts) {
        if (parts.length != 4 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use TICKET CREATE;<identifier>;<email>;<date>";
        }
        String id = parts[1];
        User user = park.getUsers().findByEmail(parts[2]);
        LocalDate date = Dates.parse(parts[3]);
        if (!park.getTickets().isIdAvailable(id)) {
            return "ERROR: Ticket identifier already exists";
        }
        if (user == null) {
            return "ERROR: User not found";
        }
        if (date == null) {
            return "ERROR: Invalid date. Use yyyy-MM-dd";
        }
        if (date.isBefore(park.getCurrentDate())) {
            return "ERROR: Tickets cannot be created for past dates";
        }
        Ticket ticket = new IndividualTicket(id, date, park.getCurrentPrice(), user);
        park.getTickets().add(ticket);
        return "TICKET CREATED - Price: " + ticket.getPrice().toPlainString() + " EUR";
    }

    String delete(String[] parts) {
        if (parts.length != 2 || CommandProcessor.hasEmpty(parts)) {
            return "ERROR: Invalid format. Use TICKET DELETE;<identifier>";
        }
        Ticket ticket = park.getTickets().findById(parts[1]);
        if (!(ticket instanceof IndividualTicket)) {
            return "ERROR: Ticket not found";
        }
        if (!ticket.isDeletable(park.getCurrentDate())) {
            return "ERROR: Only tickets dated after the current date can be deleted";
        }
        park.getTickets().remove(ticket);
        return "TICKET DELETED";
    }
}
