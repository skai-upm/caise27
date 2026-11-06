package es.upm.park.controller;

import es.upm.park.model.Group;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Central park management. Exactly one instance exists per execution; it keeps
 * the overall park state: current system date, base ticket price, groups and tickets.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_BASE_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal basePrice;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private Predicate<String> userExists;
    private Function<String, Collection<LocalDate>> groupTicketDates;

    private ParkController() {
        reset();
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the startup state. Intended for tests. */
    void reset() {
        currentDate = LocalDate.now();
        basePrice = INITIAL_BASE_PRICE;
        groups.clear();
        tickets.clear();
        userExists = email -> false;
        groupTicketDates = groupId -> List.of();
    }

    // ---- system settings -------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        if (date == null) {
            throw new ParkException("The system date is required");
        }
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new ParkException("The price must be a positive amount");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new ParkException("The price must have at most two decimals");
        }
        basePrice = price.setScale(2);
    }

    // ---- integration points with users and group tickets -------------------

    /** Sets how the existence of a registered user is checked (by email). */
    public void setUserExistsLookup(Predicate<String> lookup) {
        this.userExists = lookup;
    }

    /** Sets how the dates of the group tickets associated with a group are obtained. */
    public void setGroupTicketDatesLookup(Function<String, Collection<LocalDate>> lookup) {
        this.groupTicketDates = lookup;
    }

    // ---- groups -----------------------------------------------------------

    public Optional<Group> findGroup(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    /** A group is locked while it has a group ticket dated today or later (derived, never stored). */
    public boolean isGroupLocked(String id) {
        return groupTicketDates.apply(id).stream().anyMatch(date -> !date.isBefore(currentDate));
    }

    public Group createGroup(String id, String name) {
        requireText(id, "The group identifier is required");
        requireText(name, "The group name is required");
        if (groups.containsKey(id)) {
            throw new ParkException("A group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        return group;
    }

    public void addUserToGroup(String id, String email) {
        Group group = requireModifiableGroup(id);
        if (!userExists.test(email)) {
            throw new ParkException("No registered user with email " + email);
        }
        if (!group.addMember(email)) {
            throw new ParkException("User " + email + " already belongs to group " + id);
        }
    }

    public void removeUserFromGroup(String id, String email) {
        Group group = requireModifiableGroup(id);
        if (!group.removeMember(email)) {
            throw new ParkException("User " + email + " does not belong to group " + id);
        }
    }

    public void deleteGroup(String id) {
        requireGroup(id);
        if (isGroupLocked(id)) {
            throw new ParkException("Group " + id + " has a group ticket for today or a later date and cannot be deleted");
        }
        groups.remove(id);
    }

    // ---- tickets ------------------------------------------------------------

    public Optional<Ticket> findTicket(String id) {
        return Optional.ofNullable(tickets.get(id));
    }

    /** The individual tickets of a user, obtained by traversing the system's tickets. */
    public List<IndividualTicket> ticketsOf(String email) {
        return tickets.values().stream()
                .filter(IndividualTicket.class::isInstance)
                .map(IndividualTicket.class::cast)
                .filter(ticket -> ticket.getOwnerEmail().equals(email))
                .toList();
    }

    public IndividualTicket createIndividualTicket(String id, String email, LocalDate date) {
        requireText(id, "The ticket identifier is required");
        requireUnusedTicketId(id);
        if (!userExists.test(email)) {
            throw new ParkException("No registered user with email " + email);
        }
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, email, date, basePrice);
        tickets.put(id, ticket);
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof IndividualTicket)) {
            throw new ParkException("No individual ticket with identifier " + id);
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new ParkException("Ticket " + id + " is dated today or earlier and is kept as a historical record");
        }
        tickets.remove(id);
    }

    /** Common identifier check shared by all ticket types. */
    void requireUnusedTicketId(String id) {
        if (tickets.containsKey(id)) {
            throw new ParkException("A ticket with identifier " + id + " already exists");
        }
    }

    private void requireNotPast(LocalDate date) {
        if (date == null) {
            throw new ParkException("The ticket date is required");
        }
        if (date.isBefore(currentDate)) {
            throw new ParkException("Tickets cannot be created for a date before " + currentDate);
        }
    }

    // ---- users ---------------------------------------------------------------

    /**
     * Checks that a user can be deleted and, if so, removes them from every group.
     * Rejected when the user has an active ticket or belongs to a locked group;
     * in that case nothing changes. Historical tickets are kept.
     */
    public void deleteUserReferences(String email) {
        boolean activeTicket = ticketsOf(email).stream().anyMatch(ticket -> ticket.isActiveOn(currentDate));
        if (activeTicket) {
            throw new ParkException("User " + email + " has an active ticket and cannot be deleted");
        }
        List<Group> memberships = groups.values().stream().filter(group -> group.hasMember(email)).toList();
        for (Group group : memberships) {
            if (isGroupLocked(group.getId())) {
                throw new ParkException("User " + email + " belongs to group " + group.getId()
                        + ", which has an active group ticket");
            }
        }
        memberships.forEach(group -> group.removeMember(email));
    }

    private Group requireGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new ParkException("No group with identifier " + id);
        }
        return group;
    }

    private Group requireModifiableGroup(String id) {
        Group group = requireGroup(id);
        if (isGroupLocked(id)) {
            throw new ParkException("Group " + id + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ParkException(message);
        }
    }
}
