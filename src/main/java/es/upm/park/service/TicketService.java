package es.upm.park.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Supplier;

import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;
import es.upm.park.model.User;
import es.upm.park.repository.TicketRepository;
import es.upm.park.repository.UserRepository;

/**
 * Ticket operations. Tickets associated with a user are found by traversing
 * the stored tickets; users keep no references to tickets.
 */
public class TicketService {

    private final TicketRepository tickets;
    private final UserRepository users;
    private final Supplier<LocalDate> today;
    private final Supplier<BigDecimal> basePrice;

    public TicketService(TicketRepository tickets, UserRepository users,
            Supplier<LocalDate> today, Supplier<BigDecimal> basePrice) {
        this.tickets = tickets;
        this.users = users;
        this.today = today;
        this.basePrice = basePrice;
    }

    /**
     * Creates an individual ticket at the current base price.
     */
    public IndividualTicket createIndividual(String id, String email, LocalDate date) {
        checkNewId(id);
        User user = users.findByEmail(email).orElseThrow(() -> new UserException("no user with email " + email));
        checkNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, date, basePrice.get(), user);
        tickets.add(ticket);
        return ticket;
    }

    /**
     * Deletes an individual ticket dated strictly after the current date.
     */
    public void deleteIndividual(String id) {
        Ticket ticket = tickets.findById(id)
                .filter(IndividualTicket.class::isInstance)
                .orElseThrow(() -> new TicketException("no ticket " + id));
        checkDeletable(ticket);
        tickets.remove(ticket);
    }

    public Ticket find(String id) {
        return tickets.findById(id).orElseThrow(() -> new TicketException("no ticket " + id));
    }

    /** Whether any ticket dated on or after the current date concerns the user. */
    public boolean hasActiveTicket(User user) {
        LocalDate now = today.get();
        return tickets.findAll().stream().anyMatch(t -> t.involves(user) && t.isActive(now));
    }

    void checkNewId(String id) {
        if (id == null || id.isBlank()) {
            throw new TicketException("ticket identifier is required");
        }
        if (tickets.existsId(id)) {
            throw new TicketException("ticket " + id + " already exists");
        }
    }

    void checkNotPast(LocalDate date) {
        if (date.isBefore(today.get())) {
            throw new TicketException("ticket date " + date + " is before the current date " + today.get());
        }
    }

    void checkDeletable(Ticket ticket) {
        if (!ticket.isDeletable(today.get())) {
            throw new TicketException("ticket " + ticket.getId() + " cannot be deleted: it is not dated after "
                    + today.get());
        }
    }
}
