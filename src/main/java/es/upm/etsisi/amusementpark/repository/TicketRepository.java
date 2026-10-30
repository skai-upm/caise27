package es.upm.etsisi.amusementpark.repository;

import es.upm.etsisi.amusementpark.model.Group;
import es.upm.etsisi.amusementpark.model.GroupTicket;
import es.upm.etsisi.amusementpark.model.Ticket;
import es.upm.etsisi.amusementpark.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * All tickets of the park. Top-level entries are stand-alone individual tickets and group tickets;
 * the individual tickets inside group tickets share the same identifier space.
 */
public class TicketRepository {

    private final TicketFileStore store;
    private final List<Ticket> tickets;

    public TicketRepository(TicketFileStore store) {
        this.store = store;
        this.tickets = new ArrayList<>(store.load());
    }

    public List<Ticket> findAll() {
        return List.copyOf(tickets);
    }

    /**
     * Finds any ticket, including individual tickets that belong to a group ticket.
     */
    public Optional<Ticket> findById(String id) {
        return allTickets().filter(t -> t.getId().equals(id)).findFirst();
    }

    public boolean existsId(String id) {
        return findById(id).isPresent();
    }

    public boolean isTopLevel(Ticket ticket) {
        return tickets.contains(ticket);
    }

    public boolean hasActiveTicketFor(User user, LocalDate today) {
        return tickets.stream().anyMatch(t -> t.isActiveOn(today) && t.involves(user));
    }

    public List<GroupTicket> findByGroup(Group group) {
        return tickets.stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .filter(t -> t.getGroup() == group)
                .toList();
    }

    public boolean hasActiveGroupTicket(Group group, LocalDate today) {
        return findByGroup(group).stream().anyMatch(t -> t.isActiveOn(today));
    }

    public void add(Ticket ticket) {
        tickets.add(ticket);
        save();
    }

    public void remove(Ticket ticket) {
        tickets.remove(ticket);
        save();
    }

    public void save() {
        store.save(tickets);
    }

    private Stream<Ticket> allTickets() {
        return tickets.stream().flatMap(t -> Stream.concat(Stream.of(t),
                t.getIndividualTickets().stream().filter(i -> i != t).map(Ticket.class::cast)));
    }
}
