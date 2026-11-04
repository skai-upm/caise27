package com.parque.business.service;

import com.parque.business.model.Group;
import com.parque.business.model.GroupTicket;
import com.parque.business.model.IndividualTicket;
import com.parque.business.model.Ticket;
import com.parque.business.model.User;
import com.parque.persistence.GroupTicketRepository;
import com.parque.persistence.TicketRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Individual and group ticket operations. The current date and price are always supplied by the controller,
 * so the service never consults the real clock. Rejected operations throw {@link IllegalArgumentException}
 * and leave memory and files unchanged.
 */
public class TicketService {
    private final TicketRepository repository;
    private final GroupTicketRepository groupRepository;
    private final UserService users;
    private final GroupService groups;
    private List<IndividualTicket> tickets;
    private List<GroupTicket> groupTickets;

    public TicketService(TicketRepository repository, GroupTicketRepository groupRepository, UserService users,
            GroupService groups) throws IOException {
        this.repository = repository;
        this.groupRepository = groupRepository;
        this.users = users;
        this.groups = groups;
        this.tickets = new ArrayList<>(repository.load(users::findByDni));
        this.groupTickets = new ArrayList<>(groupRepository.load(groups::find, this::find));
    }

    /** Individual tickets, including the constituents of group tickets. */
    public List<IndividualTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public List<GroupTicket> getGroupTickets() {
        return Collections.unmodifiableList(groupTickets);
    }

    /** Individual ticket with that identifier, or null. */
    public IndividualTicket find(String id) {
        for (IndividualTicket t : tickets) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    public GroupTicket findGroupTicket(String id) {
        for (GroupTicket g : groupTickets) {
            if (g.getId().equals(id)) {
                return g;
            }
        }
        return null;
    }

    /** Any ticket (individual or group) with that identifier, or null. */
    public Ticket findAny(String id) {
        Ticket t = find(id);
        return t != null ? t : findGroupTicket(id);
    }

    /** Creates an individual ticket for a registered user and a date not earlier than {@code today}. */
    public IndividualTicket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        require(UserValidator.isNonBlank(id), "Ticket identifier must not be empty");
        require(findAny(id) == null, "A ticket with identifier " + id + " already exists");
        User user = users.findByEmail(email);
        require(user != null, "No user with email " + email);
        require(!date.isBefore(today), "Ticket date " + date + " is before the system date " + today);
        IndividualTicket ticket = new IndividualTicket(id, user, date, price);
        List<IndividualTicket> next = new ArrayList<>(tickets);
        next.add(ticket);
        saveIndividual(next);
        tickets = next;
        return ticket;
    }

    /** Deletes an individual ticket whose date is strictly later than {@code today}; constituents are refused. */
    public void delete(String id, LocalDate today) {
        IndividualTicket ticket = find(id);
        require(ticket != null, "No ticket with identifier " + id);
        GroupTicket owner = groupTicketContaining(ticket);
        require(owner == null, "Ticket " + id + " belongs to group ticket " + (owner == null ? "" : owner.getId())
                + " and can only be deleted with it");
        require(ticket.isFuture(today),
                "Ticket " + id + " cannot be deleted: its date " + ticket.getDate() + " is not after " + today);
        List<IndividualTicket> next = new ArrayList<>(tickets);
        next.remove(ticket);
        saveIndividual(next);
        tickets = next;
    }

    /** Buys a group ticket: one individual ticket per current member, discounted total. */
    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date, LocalDate today,
            BigDecimal unitPrice) {
        require(UserValidator.isNonBlank(id), "Group ticket identifier must not be empty");
        require(findAny(id) == null, "A ticket with identifier " + id + " already exists");
        Group group = groups.find(groupId);
        require(group != null, "No group with identifier " + groupId);
        List<User> members = group.getMembers();
        require(!members.isEmpty(), "Group " + groupId + " has no members");
        require(!date.isBefore(today), "Ticket date " + date + " is before the system date " + today);
        List<IndividualTicket> constituents = new ArrayList<>();
        BigDecimal share = GroupTicket.shareFor(unitPrice, members.size());
        for (int i = 0; i < members.size(); i++) {
            String constituentId = id + "-" + (i + 1);
            require(findAny(constituentId) == null, "A ticket with identifier " + constituentId + " already exists");
            constituents.add(new IndividualTicket(constituentId, members.get(i), date, share));
        }
        GroupTicket groupTicket = new GroupTicket(id, group, groupId, date, unitPrice,
                GroupTicket.totalFor(unitPrice, members.size()), constituents);
        List<IndividualTicket> nextTickets = new ArrayList<>(tickets);
        nextTickets.addAll(constituents);
        List<GroupTicket> nextGroupTickets = new ArrayList<>(groupTickets);
        nextGroupTickets.add(groupTicket);
        saveBoth(nextTickets, nextGroupTickets);
        tickets = nextTickets;
        groupTickets = nextGroupTickets;
        return groupTicket;
    }

    /** Deletes a group ticket dated strictly after {@code today}, together with its constituent tickets. */
    public void deleteGroupTicket(String id, LocalDate today) {
        GroupTicket groupTicket = findGroupTicket(id);
        require(groupTicket != null, "No group ticket with identifier " + id);
        require(groupTicket.isFuture(today), "Group ticket " + id + " cannot be deleted: its date "
                + groupTicket.getDate() + " is not after " + today);
        List<IndividualTicket> nextTickets = new ArrayList<>(tickets);
        nextTickets.removeIf(groupTicket::contains);
        List<GroupTicket> nextGroupTickets = new ArrayList<>(groupTickets);
        nextGroupTickets.remove(groupTicket);
        saveBoth(nextTickets, nextGroupTickets);
        tickets = nextTickets;
        groupTickets = nextGroupTickets;
    }

    /** Individual tickets bought directly for the user (constituents of group tickets excluded). */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (IndividualTicket t : tickets) {
            if (t.getUser() == user && groupTicketContaining(t) == null) {
                result.add(t);
            }
        }
        return result;
    }

    /** Group tickets bought by the group. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (GroupTicket g : groupTickets) {
            if (g.getGroup() == group) {
                result.add(g);
            }
        }
        return result;
    }

    /** True if the user owns an individual (or constituent) ticket dated on or after {@code today}. */
    public boolean hasCurrentOrFutureTicket(User user, LocalDate today) {
        for (IndividualTicket t : tickets) {
            if (t.getUser() == user && t.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    /** True if the group has a group ticket dated on or after {@code today}. */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (GroupTicket g : groupTickets) {
            if (g.getGroup() == group && g.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    private GroupTicket groupTicketContaining(IndividualTicket ticket) {
        for (GroupTicket g : groupTickets) {
            if (g.contains(ticket)) {
                return g;
            }
        }
        return null;
    }

    private void saveIndividual(List<IndividualTicket> state) {
        try {
            repository.save(state);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save tickets: " + e.getMessage(), e);
        }
    }

    /** Writes both files; if the second write fails the first file is restored to the current state. */
    private void saveBoth(List<IndividualTicket> nextTickets, List<GroupTicket> nextGroupTickets) {
        saveIndividual(nextTickets);
        try {
            groupRepository.save(nextGroupTickets);
        } catch (IOException e) {
            try {
                repository.save(tickets);
            } catch (IOException ignored) {
                // the original error is reported below
            }
            throw new IllegalStateException("Could not save group tickets: " + e.getMessage(), e);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
