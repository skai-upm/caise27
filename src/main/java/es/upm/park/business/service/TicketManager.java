package es.upm.park.business.service;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Ticket;
import es.upm.park.business.model.User;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.TicketFileStorage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ticket operations for standalone individual tickets and group tickets. All date
 * checks use the date supplied by the caller (the application's configured system
 * date), never the computer clock. Individual, group and constituent tickets share
 * a single identifier space.
 */
public class TicketManager {

    private final TicketFileStorage storage;
    private final GroupTicketFileStorage groupStorage;
    private Map<String, IndividualTicket> tickets = new LinkedHashMap<>();
    private Map<String, GroupTicket> groupTickets = new LinkedHashMap<>();

    private static TicketManager instance;

    /** Creates the single instance for this execution with its injected repositories and collaborators; fails if it already exists. */
    public static synchronized TicketManager initialize(TicketFileStorage storage, GroupTicketFileStorage groupStorage,
                         UserManager users, GroupManager groups) {
        if (instance != null) {
            throw new IllegalStateException("TicketManager is already initialized");
        }
        instance = new TicketManager(storage, groupStorage, users, groups);
        return instance;
    }

    /** The single instance of this execution; fails if not initialized. */
    public static synchronized TicketManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TicketManager is not initialized");
        }
        return instance;
    }

    /** Ends the current execution's instance so a new one can be initialized. */
    public static synchronized void reset() {
        instance = null;
    }

    private TicketManager(TicketFileStorage storage, GroupTicketFileStorage groupStorage,
                         UserManager users, GroupManager groups) {
        this.storage = storage;
        this.groupStorage = groupStorage;
        for (IndividualTicket t : storage.load(users::findByDni)) {
            tickets.put(t.getId(), t);
        }
        for (GroupTicket t : groupStorage.load(groups::find, users::findByDni)) {
            groupTickets.put(t.getId(), t);
        }
    }

    /** Standalone individual tickets (constituents of group tickets are not included). */
    public Collection<IndividualTicket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    public Collection<GroupTicket> getGroupTickets() {
        return Collections.unmodifiableCollection(groupTickets.values());
    }

    /** Every ticket through the common abstraction: standalone and group tickets. */
    public List<Ticket> getAllTickets() {
        List<Ticket> all = new ArrayList<>(tickets.values());
        all.addAll(groupTickets.values());
        return all;
    }

    public IndividualTicket find(String id) {
        return tickets.get(id);
    }

    /** A standalone individual ticket or a group ticket, through the common abstraction. */
    public Ticket findAnyTicket(String id) {
        Ticket t = tickets.get(id);
        return t != null ? t : groupTickets.get(id);
    }

    /** A standalone individual ticket or a constituent ticket of a group ticket. */
    public IndividualTicket findIndividualTicket(String id) {
        IndividualTicket t = tickets.get(id);
        if (t != null) {
            return t;
        }
        for (GroupTicket g : groupTickets.values()) {
            for (IndividualTicket c : g.getConstituents()) {
                if (c.getId().equals(id)) {
                    return c;
                }
            }
        }
        return null;
    }

    public GroupTicket findGroupTicket(String id) {
        return groupTickets.get(id);
    }

    /** Creates a ticket for {@code owner} dated {@code date} (not before {@code today}). */
    public IndividualTicket create(String id, User owner, LocalDate date, BigDecimal price, LocalDate today) {
        requireValid(UserValidator.isNonBlank(id), "invalid ticket identifier");
        requireUnusedId(id);
        requireNotPast(date, today);
        IndividualTicket ticket = new IndividualTicket(id, owner, date, price);
        Map<String, IndividualTicket> next = new LinkedHashMap<>(tickets);
        next.put(id, ticket);
        storage.save(next.values());
        tickets = next;
        return ticket;
    }

    /** Validates, without changing anything, that a standalone ticket can be deleted on {@code today}. */
    public IndividualTicket requireDeletable(String id, LocalDate today) {
        IndividualTicket ticket = tickets.get(id);
        if (ticket == null && isConstituentId(id)) {
            throw new IllegalArgumentException("ticket " + id
                    + " belongs to a group ticket; delete the group ticket instead");
        }
        requireValid(ticket != null, "no ticket with identifier " + id);
        requireFuture(ticket, today);
        return ticket;
    }

    /** Deletes a standalone ticket whose date is strictly later than {@code today}. */
    public void delete(String id, LocalDate today) {
        requireDeletable(id, today);
        Map<String, IndividualTicket> next = new LinkedHashMap<>(tickets);
        next.remove(id);
        storage.save(next.values());
        tickets = next;
    }

    /** Buys a group ticket for the current members of {@code group}. */
    public GroupTicket createGroupTicket(String id, Group group, LocalDate date, BigDecimal price,
                                         LocalDate today) {
        requireValid(UserValidator.isNonBlank(id), "invalid ticket identifier");
        requireValid(!group.getMembers().isEmpty(), "group " + group.getId() + " has no members");
        requireUnusedId(id);
        for (int k = 1; k <= group.getMembers().size(); k++) {
            requireUnusedId(GroupTicket.constituentId(id, k));
        }
        requireNotPast(date, today);
        GroupTicket ticket = GroupTicket.purchase(id, group, date, price);
        Map<String, GroupTicket> next = new LinkedHashMap<>(groupTickets);
        next.put(id, ticket);
        groupStorage.save(next.values());
        groupTickets = next;
        return ticket;
    }

    /** Validates, without changing anything, that a group ticket can be deleted on {@code today}. */
    public GroupTicket requireGroupDeletable(String id, LocalDate today) {
        GroupTicket ticket = groupTickets.get(id);
        requireValid(ticket != null, "no group ticket with identifier " + id);
        requireFuture(ticket, today);
        return ticket;
    }

    /** Deletes a group ticket (and its constituents) dated strictly later than {@code today}. */
    public void deleteGroupTicket(String id, LocalDate today) {
        requireGroupDeletable(id, today);
        Map<String, GroupTicket> next = new LinkedHashMap<>(groupTickets);
        next.remove(id);
        groupStorage.save(next.values());
        groupTickets = next;
    }

    /** True if {@code user} owns a standalone or constituent ticket dated on or after {@code today}. */
    public boolean hasTicketOnOrAfter(User user, LocalDate today) {
        for (IndividualTicket t : tickets.values()) {
            if (t.isOwnedBy(user) && t.isOnOrAfter(today)) {
                return true;
            }
        }
        for (GroupTicket g : groupTickets.values()) {
            if (g.isOnOrAfter(today)) {
                for (IndividualTicket c : g.getConstituents()) {
                    if (c.isOwnedBy(user)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /** True if {@code group} has a group ticket dated on or after {@code today} (group locked). */
    public boolean hasGroupTicketOnOrAfter(Group group, LocalDate today) {
        for (GroupTicket g : groupTickets.values()) {
            if (g.belongsTo(group) && g.isOnOrAfter(today)) {
                return true;
            }
        }
        return false;
    }

    private boolean isUsedId(String id) {
        return tickets.containsKey(id) || groupTickets.containsKey(id) || isConstituentId(id);
    }

    private boolean isConstituentId(String id) {
        for (GroupTicket g : groupTickets.values()) {
            for (IndividualTicket c : g.getConstituents()) {
                if (c.getId().equals(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void requireUnusedId(String id) {
        requireValid(!isUsedId(id), "a ticket with identifier " + id + " already exists");
    }

    private static void requireNotPast(LocalDate date, LocalDate today) {
        requireValid(!date.isBefore(today), "ticket date " + date + " is before the system date " + today);
    }

    private static void requireFuture(Ticket ticket, LocalDate today) {
        requireValid(ticket.getDate().isAfter(today), "ticket " + ticket.getId()
                + " can only be deleted if its date is later than the system date " + today);
    }

    private static void requireValid(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}
