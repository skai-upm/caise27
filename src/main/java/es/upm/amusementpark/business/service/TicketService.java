package es.upm.amusementpark.business.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import es.upm.amusementpark.business.exception.ConflictException;
import es.upm.amusementpark.business.exception.NotFoundException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.exception.ValidationException;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.repository.TicketRepository;

/**
 * Ticket purchase and deletion. Tickets associated with a user are always obtained by traversing
 * the tickets of the system.
 */
public class TicketService {

    private TicketRepository repository;
    private UserService users;
    private Supplier<LocalDate> today;
    private Supplier<BigDecimal> basePrice;

    private static final TicketService INSTANCE = new TicketService();

    private TicketService() {
    }

    public static TicketService getInstance() {
        return INSTANCE;
    }

    /**
     * Injects the dependencies of this controller; called during application initialization.
     */
    public TicketService configure(TicketRepository repository, UserService users, Supplier<LocalDate> today,
            Supplier<BigDecimal> basePrice) {
        this.repository = repository;
        this.users = users;
        this.today = today;
        this.basePrice = basePrice;
        return this;
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

    public Optional<IndividualTicket> findIndividualById(String id) {
        return repository.findIndividualById(id);
    }

    public List<Ticket> findAll() {
        return repository.findAll();
    }

    public IndividualTicket createIndividualTicket(String id, String email, LocalDate date)
            throws ParkException {
        requireNewIdentifier(id);
        User user = users.findByEmail(email).orElseThrow(() -> new NotFoundException("User not found"));
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, date, user, basePrice.get());
        repository.save(ticket);
        return ticket;
    }

    public IndividualTicket deleteIndividualTicket(String id) throws ParkException {
        if (repository.findById(id).orElse(null) instanceof GroupTicket) {
            throw new OperationNotAllowedException("Ticket is a group ticket; use GROUPTICKET DELETE");
        }
        IndividualTicket ticket = repository.findIndividualById(id)
                .orElseThrow(() -> new NotFoundException("Ticket not found"));
        if (ticket.isPartOfGroupTicket()) {
            throw new OperationNotAllowedException("Ticket belongs to a group ticket; delete the group ticket instead");
        }
        if (!ticket.isDeletableOn(today.get())) {
            throw new OperationNotAllowedException("Only tickets dated after the current date can be deleted");
        }
        repository.delete(id);
        return ticket;
    }

    /**
     * Buys a group ticket for the given members of the group. All identifiers (the group ticket's and
     * every derived one) are checked before anything is created.
     */
    public GroupTicket createGroupTicket(String id, Group group, List<User> members, LocalDate date)
            throws ParkException {
        requireNewIdentifier(id);
        if (members.isEmpty()) {
            throw new OperationNotAllowedException("Group has no members");
        }
        requireNotPast(date);
        for (User member : members) {
            String derived = GroupTicket.derivedId(id, member.getDni());
            if (repository.isIdentifierUsed(derived)) {
                throw new ConflictException("A ticket with this identifier already exists: " + derived);
            }
        }
        GroupTicket ticket = new GroupTicket(id, date, group, members, basePrice.get());
        repository.save(ticket);
        return ticket;
    }

    public GroupTicket deleteGroupTicket(String id) throws ParkException {
        if (!(repository.findById(id).orElse(null) instanceof GroupTicket ticket)) {
            throw new NotFoundException("Group ticket not found");
        }
        if (!ticket.isDeletableOn(today.get())) {
            throw new OperationNotAllowedException("Only group tickets dated after the current date can be deleted");
        }
        repository.delete(id);
        return ticket;
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

    void requireNewIdentifier(String id) throws ParkException {
        if (id.isEmpty()) {
            throw new ValidationException("Ticket identifier is required");
        }
        if (repository.isIdentifierUsed(id)) {
            throw new ConflictException("A ticket with this identifier already exists: " + id);
        }
    }

    void requireNotPast(LocalDate date) throws ParkException {
        if (date.isBefore(today.get())) {
            throw new OperationNotAllowedException("Tickets cannot be created for past dates");
        }
    }
}
