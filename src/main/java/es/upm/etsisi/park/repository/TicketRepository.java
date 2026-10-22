package es.upm.etsisi.park.repository;

import es.upm.etsisi.park.model.Group;
import es.upm.etsisi.park.model.GroupTicket;
import es.upm.etsisi.park.model.IndividualTicket;
import es.upm.etsisi.park.model.Ticket;
import es.upm.etsisi.park.model.User;
import es.upm.etsisi.park.persistence.TicketFileStore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * All tickets of the park. Top-level tickets are stand-alone individual tickets and group
 * tickets; the individual tickets composing a group ticket share the same identifier
 * space. Every question about which tickets belong to a user or group is answered by
 * traversing this repository.
 */
public class TicketRepository {

    private final TicketFileStore store;
    private final List<Ticket> tickets;

    public TicketRepository(TicketFileStore store, UserRepository users, GroupRepository groups) {
        this.store = store;
        this.tickets = new ArrayList<>(store.load(users::findByDni, groups::findById));
    }

    /** Finds a top-level ticket or an individual ticket inside a group ticket. */
    public Optional<Ticket> findById(String id) {
        for (Ticket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return Optional.of(ticket);
            }
            if (ticket instanceof GroupTicket groupTicket) {
                for (IndividualTicket member : groupTicket.getTickets()) {
                    if (member.getId().equals(id)) {
                        return Optional.of(member);
                    }
                }
            }
        }
        return Optional.empty();
    }

    public boolean isIdInUse(String id) {
        return findById(id).isPresent();
    }

    /** True when the ticket is a stand-alone (top-level) ticket rather than part of a group ticket. */
    public boolean isTopLevel(Ticket ticket) {
        return tickets.contains(ticket);
    }

    public List<GroupTicket> findByGroup(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    public boolean hasActiveGroupTicket(Group group, LocalDate today) {
        for (GroupTicket ticket : findByGroup(group)) {
            if (ticket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /** Whether the user holds an active ticket, stand-alone or inside a group ticket. */
    public boolean hasActiveTicket(User user, LocalDate today) {
        for (Ticket ticket : tickets) {
            if (ticket.isActiveOn(today) && ticket.getUsers().contains(user)) {
                return true;
            }
        }
        return false;
    }

    public List<Ticket> findAll() {
        return Collections.unmodifiableList(tickets);
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
}
