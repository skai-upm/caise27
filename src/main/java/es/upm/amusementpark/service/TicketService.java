package es.upm.amusementpark.service;

import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.park.Park;
import es.upm.amusementpark.repository.TicketRepository;
import es.upm.amusementpark.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Ticket purchase and deletion.
 */
public class TicketService {

    private final UserRepository users;
    private final TicketRepository tickets;

    public TicketService(UserRepository users, TicketRepository tickets) {
        this.users = users;
        this.tickets = tickets;
    }

    public IndividualTicket createTicket(String identifier, String email, LocalDate date) {
        Park park = Park.getInstance();
        tickets.ensureAvailable(List.of(identifier));
        User user = users.findByEmail(email).orElseThrow(() -> new OperationException("user not found"));
        requireNotPast(date, park.getCurrentDate());
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, park.getCurrentPrice());
        tickets.add(ticket);
        return ticket;
    }

    public void deleteTicket(String identifier) {
        Ticket ticket = tickets.findById(identifier)
                .filter(IndividualTicket.class::isInstance)
                .orElseThrow(() -> new OperationException("ticket '" + identifier + "' not found"));
        requireDeletable(ticket);
        tickets.remove(ticket);
    }

    static void requireNotPast(LocalDate date, LocalDate today) {
        if (date.isBefore(today)) {
            throw new OperationException("tickets cannot be created for past dates (current date " + today + ")");
        }
    }

    static void requireDeletable(Ticket ticket) {
        LocalDate today = Park.getInstance().getCurrentDate();
        if (!ticket.isDeletableOn(today)) {
            throw new OperationException("only tickets dated after the current date (" + today + ") can be deleted");
        }
    }
}
