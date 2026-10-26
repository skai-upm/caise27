package es.upm.parque.business.controller;

import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Ticket;
import es.upm.parque.business.model.User;
import es.upm.parque.business.model.UserValidator;
import es.upm.parque.persistence.TicketStorage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps all tickets (individual and group, current and historical) and applies the ticket rules.
 * Date rules receive the system date from the controller; this class never reads the computer clock.
 */
public class TicketManager {

    private final Map<String, Ticket> ticketsById = new LinkedHashMap<>();
    private final UserManager users;
    private final GroupManager groups;
    private final TicketStorage storage;

    public TicketManager(UserManager users, GroupManager groups, TicketStorage storage) {
        this.users = users;
        this.groups = groups;
        this.storage = storage;
        for (Ticket t : storage.load(users::findByDni, groups::find)) {
            ticketsById.put(t.getId(), t);
        }
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(ticketsById.values());
    }

    /** @return the individual or group ticket with that identifier (internal tickets are not included) */
    public Ticket find(String id) {
        return ticketsById.get(id);
    }

    /**
     * @return the individual, group or internal ticket with that identifier, or null if none exists
     */
    public Ticket findAdmission(String id) {
        Ticket ticket = ticketsById.get(id);
        return ticket != null ? ticket : findInternal(id);
    }

    public IndividualTicket create(String id, String email, LocalDate date, LocalDate today, BigDecimal price) {
        requireNewId(id);
        User user = users.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("No user with email " + email);
        }
        requireNotPast(date, today);
        IndividualTicket ticket = new IndividualTicket(id, date, price, user);
        ticketsById.put(id, ticket);
        persist();
        return ticket;
    }

    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date, LocalDate today,
            BigDecimal price) {
        requireNewId(id);
        Group group = groups.find(groupId);
        if (group == null) {
            throw new IllegalArgumentException("No group with identifier " + groupId);
        }
        if (group.getMembers().isEmpty()) {
            throw new IllegalArgumentException("Group " + groupId + " has no members");
        }
        for (int i = 1; i <= group.getMembers().size(); i++) {
            if (isIdUsed(GroupTicket.componentId(id, i))) {
                throw new IllegalArgumentException(
                    "A ticket with identifier " + GroupTicket.componentId(id, i) + " already exists");
            }
        }
        requireNotPast(date, today);
        GroupTicket ticket = new GroupTicket(id, date, group, price);
        ticketsById.put(id, ticket);
        persist();
        return ticket;
    }

    /** Deletes a standalone individual ticket dated after {@code today}. */
    public void delete(String id, LocalDate today) {
        Ticket ticket = ticketsById.get(id);
        if (ticket == null) {
            IndividualTicket internal = findInternal(id);
            if (internal != null) {
                throw new IllegalArgumentException("Ticket " + id + " belongs to group ticket "
                    + internal.getGroupTicketId() + "; use GROUPTICKET DELETE");
            }
            throw new IllegalArgumentException("No ticket with identifier " + id);
        }
        if (!(ticket instanceof IndividualTicket)) {
            throw new IllegalArgumentException("Ticket " + id + " is a group ticket; use GROUPTICKET DELETE");
        }
        remove(ticket, today);
    }

    /** Deletes a group ticket dated after {@code today}, together with its internal tickets. */
    public void deleteGroupTicket(String id, LocalDate today) {
        Ticket ticket = ticketsById.get(id);
        if (!(ticket instanceof GroupTicket)) {
            throw new IllegalArgumentException("No group ticket with identifier " + id);
        }
        remove(ticket, today);
    }

    /** @return true if the user owns an individual ticket (standalone or internal) dated on or after today */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        for (IndividualTicket t : individualTickets()) {
            if (t.isOwnedBy(user) && t.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    /** @return true if the group has a group ticket dated on or after today */
    public boolean isGroupLocked(Group group, LocalDate today) {
        for (Ticket t : ticketsById.values()) {
            if (t instanceof GroupTicket gt && gt.isForGroup(group) && gt.isCurrentOrFuture(today)) {
                return true;
            }
        }
        return false;
    }

    /** Keeps the user's tickets as history once the user has been deleted. */
    public void detachOwner(User user) {
        for (IndividualTicket t : individualTickets()) {
            if (t.isOwnedBy(user)) {
                t.detachOwner();
            }
        }
    }

    /** Keeps the group's tickets as history once the group has been deleted. */
    public void detachGroup(Group group) {
        for (Ticket t : ticketsById.values()) {
            if (t instanceof GroupTicket gt && gt.isForGroup(group)) {
                gt.detachGroup();
            }
        }
    }

    private void remove(Ticket ticket, LocalDate today) {
        if (!ticket.getDate().isAfter(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " for " + ticket.getDate()
                + " cannot be deleted: only tickets dated after " + today + " can be deleted");
        }
        ticketsById.remove(ticket.getId());
        persist();
    }

    private List<IndividualTicket> individualTickets() {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : ticketsById.values()) {
            if (t instanceof IndividualTicket it) {
                result.add(it);
            } else if (t instanceof GroupTicket gt) {
                result.addAll(gt.getComponents());
            }
        }
        return result;
    }

    private IndividualTicket findInternal(String id) {
        for (IndividualTicket t : individualTickets()) {
            if (t.getId().equals(id)) {
                return t;
            }
        }
        return null;
    }

    private boolean isIdUsed(String id) {
        return ticketsById.containsKey(id) || findInternal(id) != null;
    }

    private void requireNewId(String id) {
        if (!UserValidator.isNotBlank(id)) {
            throw new IllegalArgumentException("The ticket identifier cannot be empty");
        }
        if (isIdUsed(id)) {
            throw new IllegalArgumentException("A ticket with identifier " + id + " already exists");
        }
    }

    private static void requireNotPast(LocalDate date, LocalDate today) {
        if (date.isBefore(today)) {
            throw new IllegalArgumentException("Cannot buy a ticket for a past date (" + date + ")");
        }
    }

    private void persist() {
        storage.save(new ArrayList<>(ticketsById.values()));
    }
}
