package es.upm.amusementpark;

import java.time.LocalDate;

/**
 * Ticket purchase and deletion on the park's tickets.
 */
public class TicketService {

    private final ParkController park = ParkController.getInstance();

    public IndividualTicket createTicket(String identifier, String email, LocalDate date)
            throws TicketOperationException {
        if (identifier.isEmpty()) {
            throw new TicketOperationException("The ticket identifier cannot be empty.");
        }
        if (park.getTickets().exists(identifier)) {
            throw new TicketOperationException("A ticket with identifier " + identifier + " already exists.");
        }
        User user = park.getUsers().findByEmail(email)
                .orElseThrow(() -> new TicketOperationException("No user with email " + email + " exists."));
        checkNotPast(date);
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, park.getCurrentPrice());
        park.getTickets().save(ticket);
        return ticket;
    }

    public void deleteTicket(String identifier) throws TicketOperationException {
        Ticket ticket = park.getTickets().findById(identifier)
                .orElseThrow(() -> new TicketOperationException("No ticket with identifier " + identifier + " exists."));
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new TicketOperationException("Ticket " + identifier + " is not an individual ticket.");
        }
        if (individual.isPartOfGroupTicket()) {
            throw new TicketOperationException("Ticket " + identifier
                    + " belongs to a group ticket and can only be deleted with it.");
        }
        checkDeletable(ticket);
        park.getTickets().delete(identifier);
    }

    private void checkNotPast(LocalDate date) throws TicketOperationException {
        if (date.isBefore(park.getCurrentDate())) {
            throw new TicketOperationException("Tickets cannot be created for past dates (current date "
                    + park.getCurrentDate() + ").");
        }
    }

    private void checkDeletable(Ticket ticket) throws TicketOperationException {
        if (!ticket.isDeletableOn(park.getCurrentDate())) {
            throw new TicketOperationException("Ticket " + ticket.getIdentifier()
                    + " is for the current date or earlier and is kept as a historical record.");
        }
    }
}
