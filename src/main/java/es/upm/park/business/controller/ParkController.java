package es.upm.park.business.controller;

import es.upm.park.business.exception.BusinessRuleException;
import es.upm.park.business.exception.InvalidInputException;
import es.upm.park.business.exception.NotFoundException;
import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;
import es.upm.park.business.model.EntryRules;
import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.Pass;
import es.upm.park.business.model.SpecialPass;
import es.upm.park.business.model.SpeedPass;
import es.upm.park.business.model.Ticket;

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
    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private UserDirectory users;
    private ParkStorage storage = ParkStorage.NONE;

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
        passes.clear();
        users = email -> Optional.empty();
        storage = ParkStorage.NONE;
    }

    // ---- persistence ------------------------------------------------------------

    /** Sets where the persistent state is saved after every change. */
    public void setStorage(ParkStorage storage) {
        this.storage = storage;
    }

    /** Replaces groups, tickets and attractions with a stored state; attractions start empty. */
    public void restore(ParkState state) {
        groups.clear();
        tickets.clear();
        attractions.clear();
        state.groups().forEach(group -> groups.put(group.getId(), group));
        for (Ticket ticket : state.tickets()) {
            tickets.put(ticket.getId(), ticket);
            if (ticket instanceof GroupTicket groupTicket) {
                groupTicket.getComposition().forEach(composed -> tickets.put(composed.getId(), composed));
            }
        }
        state.attractions().forEach(attraction -> attractions.put(attraction.getId(), attraction));
        passes.clear();
        state.passes().forEach(pass -> passes.put(pass.getId(), pass));
    }

    /** The persistent part of the park state. */
    public ParkState snapshot() {
        List<Ticket> topLevel = tickets.values().stream()
                .filter(ticket -> !(ticket instanceof IndividualTicket individual && individual.isPartOfGroupTicket()))
                .toList();
        return new ParkState(List.copyOf(groups.values()), topLevel, List.copyOf(attractions.values()),
                List.copyOf(passes.values()));
    }

    private void save() {
        storage.save(snapshot());
    }

    // ---- system settings -------------------------------------------------

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(LocalDate date) {
        if (date == null) {
            throw new InvalidInputException("The system date is required");
        }
        currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new InvalidInputException("The price must be a positive amount");
        }
        if (price.stripTrailingZeros().scale() > 2) {
            throw new InvalidInputException("The price must have at most two decimals");
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

    /** Group tickets bought by a group, obtained by traversing the system's tickets. */
    public List<GroupTicket> groupTicketsOf(Group group) {
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
            throw new BusinessRuleException("A group with identifier " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        save();
        return group;
    }

    public void addUserToGroup(String id, String email) {
        Group group = requireModifiableGroup(id);
        if (!users.exists(email)) {
            throw new NotFoundException("No registered user with email " + email);
        }
        if (!group.addMember(email)) {
            throw new BusinessRuleException("User " + email + " already belongs to group " + id);
        }
        save();
    }

    public void removeUserFromGroup(String id, String email) {
        Group group = requireModifiableGroup(id);
        if (!group.removeMember(email)) {
            throw new BusinessRuleException("User " + email + " does not belong to group " + id);
        }
        save();
    }

    /** Deletes a group; its historical group tickets are kept without a group reference. */
    public void deleteGroup(String id) {
        Group group = requireGroup(id);
        if (isGroupLocked(id)) {
            throw new BusinessRuleException("Group " + id + " has a group ticket for today or a later date and cannot be deleted");
        }
        groupTicketsOf(group).forEach(GroupTicket::detachGroup);
        groups.remove(id);
        save();
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
            throw new NotFoundException("No registered user with email " + email);
        }
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, email, date, basePrice);
        tickets.put(id, ticket);
        save();
        return ticket;
    }

    public void deleteTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new NotFoundException("No individual ticket with identifier " + id);
        }
        if (individual.isPartOfGroupTicket()) {
            throw new BusinessRuleException("Ticket " + id + " belongs to a group ticket and can only be deleted with it");
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new BusinessRuleException("Ticket " + id + " is dated today or earlier and is kept as a historical record");
        }
        removePassesOf(List.of(individual));
        tickets.remove(id);
        save();
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
            throw new BusinessRuleException("Group " + groupId + " has no members");
        }
        requireNotPast(date);
        requireUnusedTicketId(id);
        Map<String, String> memberDnis = new LinkedHashMap<>();
        Set<String> derivedIds = new HashSet<>();
        for (String email : group.getMemberEmails()) {
            String dni = users.dniOf(email)
                    .orElseThrow(() -> new NotFoundException("No registered user with email " + email));
            String derivedId = GroupTicket.composedId(id, dni);
            requireUnusedTicketId(derivedId);
            if (!derivedIds.add(derivedId)) {
                throw new BusinessRuleException("Duplicate ticket identifier " + derivedId);
            }
            memberDnis.put(email, dni);
        }
        GroupTicket groupTicket = GroupTicket.purchase(id, group, date, basePrice, memberDnis);
        tickets.put(id, groupTicket);
        groupTicket.getComposition().forEach(ticket -> tickets.put(ticket.getId(), ticket));
        save();
        return groupTicket;
    }

    /** Deletes a strictly future group ticket together with its composed tickets. */
    public void deleteGroupTicket(String id) {
        if (!(tickets.get(id) instanceof GroupTicket groupTicket)) {
            throw new NotFoundException("No group ticket with identifier " + id);
        }
        if (!groupTicket.isDeletableOn(currentDate)) {
            throw new BusinessRuleException("Group ticket " + id + " is dated today or earlier and is kept as a historical record");
        }
        removePassesOf(groupTicket.getComposition());
        groupTicket.getComposition().forEach(ticket -> tickets.remove(ticket.getId()));
        tickets.remove(id);
        save();
    }

    // ---- passes ---------------------------------------------------------------

    public Optional<Pass> findPass(String id) {
        return Optional.ofNullable(passes.get(id));
    }

    /** Passes associated with individual tickets owned by a user, found by traversal. */
    public List<Pass> passesOf(String email) {
        return passes.values().stream().filter(pass -> pass.getTicket().getOwnerEmail().equals(email)).toList();
    }

    /** Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:1..n}; nothing is created on any conflict. */
    public List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, int quantity) {
        requireText(purchaseId, "The SpeedPass purchase identifier is required");
        if (quantity != 1 && quantity != 10) {
            throw new InvalidInputException("SpeedPasses are sold in quantities of 1 or 10");
        }
        IndividualTicket ticket = requireTicketForPass(ticketId);
        List<SpeedPass> bought = new java.util.ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String passId = purchaseId + ":" + n;
            requireUnusedPassId(passId);
            bought.add(new SpeedPass(passId, ticket));
        }
        bought.forEach(pass -> passes.put(pass.getId(), pass));
        save();
        return bought;
    }

    /** Buys the Special Pass of an individual ticket (at most one, even once consumed). */
    public SpecialPass buySpecialPass(String id, String ticketId) {
        requireText(id, "The Special Pass identifier is required");
        requireUnusedPassId(id);
        IndividualTicket ticket = requireTicketForPass(ticketId);
        boolean alreadyHasOne = passes.values().stream()
                .anyMatch(pass -> pass instanceof SpecialPass && pass.getTicket() == ticket);
        if (alreadyHasOne) {
            throw new BusinessRuleException("Ticket " + ticketId + " already has a Special Pass");
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        passes.put(id, pass);
        save();
        return pass;
    }

    /** Refunds (deletes) an unconsumed pass of the given kind whose ticket is dated after today. */
    public void refundPass(String id, Class<? extends Pass> kind) {
        Pass pass = passes.get(id);
        if (!kind.isInstance(pass)) {
            throw new NotFoundException("No " + kind.getSimpleName() + " with identifier " + id);
        }
        if (pass.isConsumed()) {
            throw new BusinessRuleException("Pass " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(currentDate)) {
            throw new BusinessRuleException("Pass " + id + " belongs to a ticket for today or earlier and cannot be refunded");
        }
        passes.remove(id);
        save();
    }

    private IndividualTicket requireTicketForPass(String ticketId) {
        if (!(tickets.get(ticketId) instanceof IndividualTicket ticket)) {
            throw new NotFoundException("No individual ticket with identifier " + ticketId);
        }
        if (ticket.getDate().isBefore(currentDate)) {
            throw new BusinessRuleException("Ticket " + ticketId + " is dated before " + currentDate);
        }
        return ticket;
    }

    private void requireUnusedPassId(String id) {
        if (passes.containsKey(id)) {
            throw new BusinessRuleException("A pass with identifier " + id + " already exists");
        }
    }

    private void removePassesOf(List<IndividualTicket> deletedTickets) {
        passes.values().removeIf(pass -> deletedTickets.contains(pass.getTicket()));
    }

    /** Common identifier check shared by all ticket types. */
    void requireUnusedTicketId(String id) {
        if (tickets.containsKey(id)) {
            throw new BusinessRuleException("A ticket with identifier " + id + " already exists");
        }
    }

    private void requireNotPast(LocalDate date) {
        if (date == null) {
            throw new InvalidInputException("The ticket date is required");
        }
        if (date.isBefore(currentDate)) {
            throw new BusinessRuleException("Tickets cannot be created for a date before " + currentDate);
        }
    }

    // ---- attractions -----------------------------------------------------------

    public Optional<Attraction<?>> findAttraction(String id) {
        return Optional.ofNullable(attractions.get(id));
    }

    public Attraction<?> createAttraction(String id, String name, int capacity) {
        return createAttraction(id, name, capacity, AttractionType.GENERAL);
    }

    public Attraction<?> createAttraction(String id, String name, int capacity, AttractionType type) {
        requireText(id, "The attraction identifier is required");
        requireText(name, "The attraction name is required");
        if (capacity <= 0) {
            throw new InvalidInputException("The attraction capacity must be a positive number");
        }
        if (attractions.containsKey(id)) {
            throw new BusinessRuleException("An attraction with identifier " + id + " already exists");
        }
        Attraction<?> attraction = type.create(id, name, capacity);
        attractions.put(id, attraction);
        save();
        return attraction;
    }

    /**
     * Admits the users of a ticket to an attraction, whatever the ticket type.
     * Every check runs before anyone enters, so entry is all-or-nothing.
     *
     * @return the emails of the users who entered
     */
    public List<String> enterAttraction(String attractionId, String ticketId) {
        Attraction<?> attraction = requireAttraction(attractionId);
        Ticket ticket = findTicket(ticketId)
                .orElseThrow(() -> new NotFoundException("No ticket with identifier " + ticketId));
        return attraction.admit(ticket, currentDate, entryRules());
    }

    /** Park information used by attraction-specific entry rules. */
    private EntryRules entryRules() {
        return ticket -> passes.values().stream()
                .anyMatch(pass -> pass instanceof SpecialPass && pass.getTicket() == ticket && !pass.isConsumed());
    }

    /** Removes a user who is inside an attraction, releasing their place. */
    public void exitAttraction(String attractionId, String email) {
        Attraction<?> attraction = requireAttraction(attractionId);
        if (!attraction.leave(email)) {
            throw new BusinessRuleException("User " + email + " is not inside attraction " + attractionId);
        }
    }

    public Attraction<?> requireAttraction(String id) {
        return findAttraction(id).orElseThrow(() -> new NotFoundException("No attraction with identifier " + id));
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
            throw new BusinessRuleException("User " + email + " has an active ticket and cannot be deleted");
        }
        List<Group> memberships = groups.values().stream().filter(group -> group.hasMember(email)).toList();
        for (Group group : memberships) {
            if (isGroupLocked(group.getId())) {
                throw new BusinessRuleException("User " + email + " belongs to group " + group.getId()
                        + ", which has an active group ticket");
            }
        }
        memberships.forEach(group -> group.removeMember(email));
        save();
    }

    /** The DNI of an active registered user. */
    public String userDni(String email) {
        return users.dniOf(email)
                .orElseThrow(() -> new NotFoundException("No registered user with email " + email));
    }

    public Group requireGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new NotFoundException("No group with identifier " + id);
        }
        return group;
    }

    private Group requireModifiableGroup(String id) {
        Group group = requireGroup(id);
        if (isGroupLocked(id)) {
            throw new BusinessRuleException("Group " + id + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidInputException(message);
        }
    }
}
