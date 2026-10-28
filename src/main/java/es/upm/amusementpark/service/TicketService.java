package es.upm.amusementpark.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import es.upm.amusementpark.model.IndividualTicket;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import es.upm.amusementpark.repository.TicketRepository;

/**
 * Ticket purchase and deletion. Tickets associated with a user are always obtained by traversing
 * the tickets of the system.
 */
public class TicketService {

    private final TicketRepository repository;
    private final UserService users;
    private final Supplier<LocalDate> today;
    private final Supplier<BigDecimal> basePrice;

    public TicketService(TicketRepository repository, UserService users, Supplier<LocalDate> today,
            Supplier<BigDecimal> basePrice) {
        this.repository = repository;
        this.users = users;
        this.today = today;
        this.basePrice = basePrice;
    }

    public Optional<Ticket> findById(String id) {
        return repository.findById(id);
    }

    public List<Ticket> findAll() {
        return repository.findAll();
    }

    public IndividualTicket createIndividualTicket(String id, String email, LocalDate date)
            throws OperationException {
        requireNewIdentifier(id);
        User user = users.findByEmail(email).orElseThrow(() -> new OperationException("User not found"));
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, date, user, basePrice.get());
        repository.save(ticket);
        return ticket;
    }

    public void deleteIndividualTicket(String id) throws OperationException {
        IndividualTicket ticket = repository.findIndividualById(id)
                .orElseThrow(() -> new OperationException("Ticket not found"));
        if (ticket.isPartOfGroupTicket()) {
            throw new OperationException("Ticket belongs to a group ticket; delete the group ticket instead");
        }
        if (!ticket.isDeletableOn(today.get())) {
            throw new OperationException("Only tickets dated after the current date can be deleted");
        }
        repository.delete(id);
    }

    /**
     * Tells whether the user has a ticket (standalone or in a group ticket) dated today or later.
     */
    public boolean hasActiveTicket(String dni) {
        LocalDate now = today.get();
        return repository.allIndividualTickets().stream()
                .anyMatch(t -> t.getUser().getDni().equals(dni) && t.isActiveOn(now));
    }

    void requireNewIdentifier(String id) throws OperationException {
        if (id.isEmpty()) {
            throw new OperationException("Ticket identifier is required");
        }
        if (repository.isIdentifierUsed(id)) {
            throw new OperationException("A ticket with this identifier already exists: " + id);
        }
    }

    void requireNotPast(LocalDate date) throws OperationException {
        if (date.isBefore(today.get())) {
            throw new OperationException("Tickets cannot be created for past dates");
        }
    }
}
