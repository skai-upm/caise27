package es.upm.park.controller;

import es.upm.park.model.Attraction;
import es.upm.park.model.Group;
import es.upm.park.model.GroupTicket;
import es.upm.park.model.IndividualTicket;
import es.upm.park.model.Ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central park management. Exactly one instance exists per execution; it keeps
 * the overall park state: current system date, base ticket price, groups,
 * tickets and attractions.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_BASE_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal basePrice;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private UserDirectory users;

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
        attractions.clear();
        users = email -> Optional.empty();
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

    // ---- integration point with user management ------------------------------

    /** Sets the source of registered users (existence and DNI, by email). */
    public void setUserDirectory(UserDirectory directory) {
        this.users = directory;
    }

    // ---- groups -----------------------------------------------------------

    public Optional<Group> findGroup(String id) {
        return Optional.ofNullable(groups.get(id));
    }

    /** A group is locked while it has a group ticket dated today or later (derived, never stored). */
    public boolean isGroupLocked(String id) {
        Group group = groups.get(id);
        return group != null && groupTicketsOf(group).stream().anyMatch(ticket -> ticket.isActiveOn(currentDate));
    }

    private List<GroupTicket> groupTicketsOf(Group group) {
        return tickets.values().stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .filter(ticket -> ticket.getGroup() == group)
                .toList();
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
        if (!users.exists(email)) {
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

    /** Deletes a group; its historical group tickets are kept without a group reference. */
    public void deleteGroup(String id) {
        Group group = requireGroup(id);
        if (isGroupLocked(id)) {
            throw new ParkException("Group " + id + " has a group ticket for today or a later date and cannot be deleted");
        }
        groupTicketsOf(group).forEach(GroupTicket::detachGroup);
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
        if (!users.exists(email)) {
            throw new ParkException("No registered user with email " + email);
        }
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, email, date, basePrice);
        tickets.put(id, ticket);
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new ParkException("No individual ticket with identifier " + id);
        }
        if (individual.isPartOfGroupTicket()) {
            throw new ParkException("Ticket " + id + " belongs to a group ticket and can only be deleted with it");
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new ParkException("Ticket " + id + " is dated today or earlier and is kept as a historical record");
        }
        tickets.remove(id);
    }

    /**
     * Buys a group ticket for the current members of a non-empty group. All
     * identifiers (the group ticket and its composed tickets) are checked first,
     * so nothing is created if any of them conflicts.
     */
    public GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        requireText(id, "The ticket identifier is required");
        Group group = requireGroup(groupId);
        if (group.getMemberEmails().isEmpty()) {
            throw new ParkException("Group " + groupId + " has no members");
        }
        requireNotPast(date);
        requireUnusedTicketId(id);
        Map<String, String> memberDnis = new LinkedHashMap<>();
        Set<String> derivedIds = new HashSet<>();
        for (String email : group.getMemberEmails()) {
            String dni = users.dniOf(email)
                    .orElseThrow(() -> new ParkException("No registered user with email " + email));
            String derivedId = GroupTicket.composedId(id, dni);
            requireUnusedTicketId(derivedId);
            if (!derivedIds.add(derivedId)) {
                throw new ParkException("Duplicate ticket identifier " + derivedId);
            }
            memberDnis.put(email, dni);
        }
        GroupTicket groupTicket = GroupTicket.purchase(id, group, date, basePrice, memberDnis);
        tickets.put(id, groupTicket);
        groupTicket.getComposition().forEach(ticket -> tickets.put(ticket.getId(), ticket));
        return groupTicket;
    }

    /** Deletes a strictly future group ticket together with its composed tickets. */
    public void deleteGroupTicket(String id) {
        if (!(tickets.get(id) instanceof GroupTicket groupTicket)) {
            throw new ParkException("No group ticket with identifier " + id);
        }
        if (!groupTicket.isDeletableOn(currentDate)) {
            throw new ParkException("Group ticket " + id + " is dated today or earlier and is kept as a historical record");
        }
        groupTicket.getComposition().forEach(ticket -> tickets.remove(ticket.getId()));
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

    // ---- attractions -----------------------------------------------------------

    public Optional<Attraction> findAttraction(String id) {
        return Optional.ofNullable(attractions.get(id));
    }

    public Attraction createAttraction(String id, String name, int capacity) {
        requireText(id, "The attraction identifier is required");
        requireText(name, "The attraction name is required");
        if (capacity <= 0) {
            throw new ParkException("The attraction capacity must be a positive number");
        }
        if (attractions.containsKey(id)) {
            throw new ParkException("An attraction with identifier " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.put(id, attraction);
        return attraction;
    }

    /**
     * Admits the users of a ticket to an attraction, whatever the ticket type.
     * Every check runs before anyone enters, so entry is all-or-nothing.
     *
     * @return the emails of the users who entered
     */
    public List<String> enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId)
                .orElseThrow(() -> new ParkException("No ticket with identifier " + ticketId));
        if (!ticket.isValidOn(currentDate)) {
            throw new ParkException("Ticket " + ticketId + " is not valid on " + currentDate);
        }
        List<String> entrants = ticket.admittedUsers();
        for (String email : entrants) {
            if (attraction.isInside(email)) {
                throw new ParkException("User " + email + " is already inside attraction " + attractionId);
            }
        }
        int free = attraction.getCapacity() - attraction.getOccupantEmails().size();
        if (entrants.size() > free) {
            throw new ParkException("Attraction " + attractionId + " has " + free + " free places for "
                    + entrants.size() + " users");
        }
        entrants.forEach(attraction::enter);
        return entrants;
    }

    public Attraction requireAttraction(String id) {
        return findAttraction(id).orElseThrow(() -> new ParkException("No attraction with identifier " + id));
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
