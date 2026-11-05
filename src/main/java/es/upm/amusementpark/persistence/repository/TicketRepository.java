package es.upm.amusementpark.persistence.repository;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.exception.RuleViolationException;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.persistence.storage.TicketStorage;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Stores every ticket of the park in a single identifier space shared by all ticket types.
 */
public class TicketRepository {

    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final TicketStorage storage;

    public TicketRepository() {
        this(TicketStorage.none(), new UserRepository(), new GroupRepository());
    }

    public TicketRepository(TicketStorage storage, UserRepository users, GroupRepository groups) {
        this.storage = storage;
        storage.load(users, groups).forEach(t -> tickets.put(t.getIdentifier(), t));
    }

    /** Common uniqueness check for any ticket type; throws if any identifier is already used. */
    public void ensureAvailable(Collection<String> identifiers) {
        for (String id : identifiers) {
            if (id.isEmpty()) {
                throw new InvalidDataException("ticket identifier must not be empty");
            }
            if (tickets.containsKey(id)) {
                throw new RuleViolationException("a ticket with identifier '" + id + "' already exists");
            }
        }
        if (identifiers.stream().distinct().count() != identifiers.size()) {
            throw new RuleViolationException("duplicate ticket identifiers");
        }
    }

    public void add(Ticket ticket) {
        addAll(List.of(ticket));
    }

    /** Adds several tickets at once (all identifiers are checked first). */
    public void addAll(List<? extends Ticket> newTickets) {
        ensureAvailable(newTickets.stream().map(Ticket::getIdentifier).toList());
        newTickets.forEach(t -> tickets.put(t.getIdentifier(), t));
        save();
    }

    public void remove(Ticket ticket) {
        removeAll(List.of(ticket));
    }

    public void removeAll(List<? extends Ticket> oldTickets) {
        oldTickets.forEach(t -> tickets.remove(t.getIdentifier()));
        save();
    }

    /** Persists changes made to stored tickets or to the data they reference. */
    public void update() {
        save();
    }

    private void save() {
        storage.save(tickets.values());
    }

    public Optional<Ticket> findById(String identifier) {
        return Optional.ofNullable(tickets.get(identifier));
    }

    public List<Ticket> findAll() {
        return List.copyOf(tickets.values());
    }

    /** Individual tickets of a user, found by traversing all tickets. */
    public List<IndividualTicket> individualTicketsOf(User user) {
        return tickets.values().stream()
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .filter(t -> t.getUser() == user)
                .toList();
    }

    /** Group tickets purchased by a group, found by traversal. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        return groupTickets().stream().filter(t -> t.getGroup() == group).toList();
    }

    /** A group is locked while it has an active group ticket. */
    public boolean isLocked(Group group, LocalDate today) {
        return groupTicketsOf(group).stream().anyMatch(t -> t.isActiveOn(today));
    }

    /** The group ticket whose composition contains the given ticket, if any. */
    public Optional<GroupTicket> containerOf(IndividualTicket ticket) {
        return groupTickets().stream().filter(g -> g.contains(ticket)).findFirst();
    }

    public List<GroupTicket> groupTickets() {
        return tickets.values().stream().filter(GroupTicket.class::isInstance).map(GroupTicket.class::cast).toList();
    }
}
