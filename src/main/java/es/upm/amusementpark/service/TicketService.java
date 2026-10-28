package es.upm.amusementpark.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.GroupTicket;
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

    /**
     * Finds a ticket by identifier, including individual tickets inside group tickets.
     */
    public Optional<Ticket> findAnyById(String id) {
        Optional<Ticket> ticket = repository.findById(id);
        return ticket.isPresent() ? ticket : repository.findIndividualById(id).map(Ticket.class::cast);
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
        if (repository.findById(id).orElse(null) instanceof GroupTicket) {
            throw new OperationException("Ticket is a group ticket; use GROUPTICKET DELETE");
        }
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
     * Buys a group ticket for the given members of the group. All identifiers (the group ticket's and
     * every derived one) are checked before anything is created.
     */
    public GroupTicket createGroupTicket(String id, Group group, List<User> members, LocalDate date)
            throws OperationException {
        requireNewIdentifier(id);
        if (members.isEmpty()) {
            throw new OperationException("Group has no members");
        }
        requireNotPast(date);
        for (User member : members) {
            String derived = GroupTicket.derivedId(id, member.getDni());
            if (repository.isIdentifierUsed(derived)) {
                throw new OperationException("A ticket with this identifier already exists: " + derived);
            }
        }
        GroupTicket ticket = new GroupTicket(id, date, group, members, basePrice.get());
        repository.save(ticket);
        return ticket;
    }

    public void deleteGroupTicket(String id) throws OperationException {
        if (!(repository.findById(id).orElse(null) instanceof GroupTicket ticket)) {
            throw new OperationException("Group ticket not found");
        }
        if (!ticket.isDeletableOn(today.get())) {
            throw new OperationException("Only group tickets dated after the current date can be deleted");
        }
        repository.delete(id);
    }

    /**
     * Tells whether the group has a group ticket dated on or after the given day.
     */
    public boolean hasActiveGroupTicket(String groupId, LocalDate day) {
        return repository.findAll().stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .anyMatch(t -> t.getGroup() != null && t.getGroup().getId().equals(groupId) && t.isActiveOn(day));
    }

    /**
     * Clears the group reference of the tickets bought by a deleted group; they stay as records.
     */
    public void detachGroup(Group group) {
        repository.findAll().stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .filter(t -> t.getGroup() == group)
                .forEach(GroupTicket::detachGroup);
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
