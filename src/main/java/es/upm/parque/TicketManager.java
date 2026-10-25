package es.upm.parque;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Keeps individual and group tickets, answers date-based questions about them and keeps the repository up to
 * date. Internal tickets of a group ticket live inside it, not in the individual list.
 */
public class TicketManager {

    private final TicketRepository repository;
    private final List<IndividualTicket> individualTickets;
    private final List<GroupTicket> groupTickets;

    public TicketManager(TicketRepository repository, Function<String, User> userByDni,
            Function<String, Group> groupById) throws IOException {
        this.repository = repository;
        TicketRepository.Loaded loaded = repository.load(userByDni, groupById);
        this.individualTickets = new ArrayList<>(loaded.individualTickets());
        this.groupTickets = new ArrayList<>(loaded.groupTickets());
    }

    public List<IndividualTicket> getIndividualTickets() {
        return List.copyOf(individualTickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return List.copyOf(groupTickets);
    }

    /**
     * Finds an individual ticket, an internal ticket of a group ticket or a group ticket by identifier.
     */
    public Ticket find(String id) {
        for (Ticket ticket : allTickets()) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        return null;
    }

    public boolean isIdTaken(String id) {
        return find(id) != null;
    }

    /**
     * Returns the group ticket that contains the given ticket, or {@code null} for an individually bought one.
     */
    public GroupTicket groupTicketOf(IndividualTicket ticket) {
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.contains(ticket)) {
                return groupTicket;
            }
        }
        return null;
    }

    public boolean add(IndividualTicket ticket) {
        individualTickets.add(ticket);
        if (!persist()) {
            individualTickets.remove(ticket);
            return false;
        }
        return true;
    }

    public boolean add(GroupTicket groupTicket) {
        groupTickets.add(groupTicket);
        if (!persist()) {
            groupTickets.remove(groupTicket);
            return false;
        }
        return true;
    }

    public boolean delete(IndividualTicket ticket) {
        individualTickets.remove(ticket);
        return persist();
    }

    public boolean delete(GroupTicket groupTicket) {
        groupTickets.remove(groupTicket);
        return persist();
    }

    /**
     * True if the user holds any ticket, individual or internal, dated on or after {@code today}.
     */
    public boolean hasCurrentOrFutureTicket(User user, LocalDate today) {
        for (IndividualTicket ticket : ticketsOf(user)) {
            if (ticket.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * True if the group has any group ticket dated on or after {@code today}.
     */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (GroupTicket groupTicket : groupTickets) {
            if (groupTicket.getGroup() == group && groupTicket.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Every ticket held by the user, including internal tickets of group tickets.
     */
    public List<IndividualTicket> ticketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (IndividualTicket ticket : individualTickets) {
            if (ticket.getUser() == user) {
                result.add(ticket);
            }
        }
        for (GroupTicket groupTicket : groupTickets) {
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                if (ticket.getUser() == user) {
                    result.add(ticket);
                }
            }
        }
        return result;
    }

    /**
     * Deletes every ticket of the user, removing internal ones from their group tickets.
     */
    public boolean removeTicketsOf(User user) {
        individualTickets.removeIf(ticket -> ticket.getUser() == user);
        for (GroupTicket groupTicket : groupTickets) {
            for (IndividualTicket ticket : groupTicket.getTickets()) {
                if (ticket.getUser() == user) {
                    groupTicket.removeTicket(ticket);
                }
            }
        }
        return persist();
    }

    /**
     * Deletes every group ticket of the group together with its internal tickets.
     */
    public boolean removeGroupTicketsOf(Group group) {
        groupTickets.removeIf(groupTicket -> groupTicket.getGroup() == group);
        return persist();
    }

    private List<Ticket> allTickets() {
        List<Ticket> all = new ArrayList<>(individualTickets);
        for (GroupTicket groupTicket : groupTickets) {
            all.add(groupTicket);
            all.addAll(groupTicket.getTickets());
        }
        return all;
    }

    private boolean persist() {
        try {
            repository.saveAll(individualTickets, groupTickets);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
