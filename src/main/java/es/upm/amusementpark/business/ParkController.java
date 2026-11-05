package es.upm.amusementpark.business;

import es.upm.amusementpark.business.invoice.GroupInvoiceBuilder;
import es.upm.amusementpark.business.invoice.Invoice;
import es.upm.amusementpark.business.invoice.UserInvoiceBuilder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Central, single-instance manager of the overall park state during one execution.
 */
public final class ParkController {

    /** Base ticket price at the start of every execution. */
    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal currentPrice;
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private UserDirectory userDirectory = UserDirectory.EMPTY;
    private final GroupTicketDates registeredGroupTicketDates = this::registeredGroupTicketDates;
    private GroupTicketDates groupTicketDates = registeredGroupTicketDates;
    private ParkRepository repository = ParkRepository.NONE;
    private PasswordHasher passwordHasher = new BCryptPasswordHasher();

    private ParkController() {
        reset();
    }

    /**
     * Returns the only central park management instance.
     *
     * @return the singleton instance
     */
    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the start-of-execution state: today's local date, the initial base price and no groups. */
    public synchronized void reset() {
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
        groups.clear();
        tickets.clear();
        attractions.clear();
        userDirectory = UserDirectory.EMPTY;
        groupTicketDates = registeredGroupTicketDates;
        repository = ParkRepository.NONE;
        passwordHasher = new BCryptPasswordHasher();
    }

    /** Injects the password protection behavior configured at application initialization. */
    public synchronized void setPasswordHasher(PasswordHasher hasher) {
        passwordHasher = Objects.requireNonNull(hasher, "hasher");
    }

    /**
     * Protects a validated password before storing it; a new value is produced on every call.
     *
     * @return the protected value to store
     */
    public synchronized String protectPassword(String plainPassword) {
        return passwordHasher.protect(plainPassword);
    }

    /** @return whether the entered password matches the stored protected value */
    public synchronized boolean verifyPassword(String plainPassword, String storedValue) {
        return passwordHasher.verify(plainPassword, storedValue);
    }

    /** Injects the persistence dependency configured at application initialization. */
    public synchronized void setRepository(ParkRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /**
     * Rebuilds the persisted state through the injected repository.
     *
     * @return warnings about records that could not be interpreted
     * @throws RepositoryException if the state cannot be read
     */
    public synchronized List<String> loadState() {
        return repository.load(this);
    }

    /**
     * Stores the current state through the injected repository.
     *
     * @throws RepositoryException if the state cannot be written
     */
    public synchronized void saveState() {
        repository.save(this);
    }

    public synchronized void setUserDirectory(UserDirectory directory) {
        userDirectory = Objects.requireNonNull(directory, "directory");
    }

    public synchronized void setGroupTicketDates(GroupTicketDates provider) {
        groupTicketDates = Objects.requireNonNull(provider, "provider");
    }

    public synchronized LocalDate getCurrentDate() {
        return currentDate;
    }

    public synchronized void setCurrentDate(LocalDate date) {
        currentDate = Objects.requireNonNull(date, "date");
    }

    public synchronized BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Sets the base ticket price for tickets created from now on.
     *
     * @param price strictly positive amount
     */
    public synchronized void setCurrentPrice(BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        currentPrice = price.setScale(2, RoundingMode.UNNECESSARY);
    }

    // ----- groups -----

    /**
     * Creates an empty group.
     *
     * @throws ParkException if a field is empty or the identifier is already used
     */
    public synchronized Group createGroup(String id, String name) {
        if (isBlank(id) || isBlank(name)) {
            throw new ParkException("Group identifier and name are required");
        }
        if (groups.containsKey(id)) {
            throw new ParkException("Group " + id + " already exists");
        }
        Group group = new Group(id, name);
        groups.put(id, group);
        return group;
    }

    public synchronized Group findGroup(String id) {
        return groups.get(id);
    }

    public synchronized Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    /**
     * A group is locked while at least one of its group tickets is dated today or later. Never stored.
     */
    public synchronized boolean isGroupLocked(String id) {
        for (LocalDate date : groupTicketDates.datesOf(id)) {
            if (!date.isBefore(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** Adds an existing user to an unlocked group. */
    public synchronized void addUserToGroup(String id, String email) {
        Group group = modifiableGroup(id);
        if (!userDirectory.exists(email)) {
            throw new ParkException("User " + email + " does not exist");
        }
        if (!group.addMember(email)) {
            throw new ParkException("User " + email + " already belongs to group " + id);
        }
    }

    /** Removes a member from an unlocked group. */
    public synchronized void removeUserFromGroup(String id, String email) {
        Group group = modifiableGroup(id);
        if (!group.removeMember(email)) {
            throw new ParkException("User " + email + " does not belong to group " + id);
        }
    }

    /** Deletes an unlocked group; its historical group tickets are kept with an empty group reference. */
    public synchronized void deleteGroup(String id) {
        Group group = modifiableGroup(id);
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                groupTicket.clearGroup();
            }
        }
        groups.remove(id);
    }

    private List<LocalDate> registeredGroupTicketDates(String groupId) {
        List<LocalDate> dates = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() != null
                    && groupTicket.getGroup().getId().equals(groupId)) {
                dates.add(ticket.getDate());
            }
        }
        return dates;
    }

    private Group modifiableGroup(String id) {
        Group group = groups.get(id);
        if (group == null) {
            throw new ParkException("Group " + id + " does not exist");
        }
        if (isGroupLocked(id)) {
            throw new ParkException("Group " + id + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    private static boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    // ----- tickets -----

    public synchronized Ticket findTicket(String id) {
        return tickets.get(id);
    }

    public synchronized Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    /**
     * Registers a ticket of any type, enforcing the shared identifier space.
     *
     * @throws ParkException if the identifier is empty or already used by any ticket
     */
    synchronized void registerTicket(Ticket ticket) {
        ensureTicketIdAvailable(ticket.getId());
        tickets.put(ticket.getId(), ticket);
    }

    /** Checks that {@code id} is non-empty and unused in the shared ticket identifier space. */
    synchronized void ensureTicketIdAvailable(String id) {
        if (isBlank(id)) {
            throw new ParkException("Ticket identifier is required");
        }
        if (tickets.containsKey(id)) {
            throw new ParkException("Ticket " + id + " already exists");
        }
    }

    /** Checks that {@code date} is not earlier than the current system date. */
    synchronized void ensureNotPast(LocalDate date) {
        if (date.isBefore(currentDate)) {
            throw new ParkException("Tickets cannot be created for past dates");
        }
    }

    /**
     * Creates an individual ticket at the current base price.
     *
     * @throws ParkException if the identifier is used, the user does not exist or the date is in the past
     */
    public synchronized IndividualTicket createIndividualTicket(String id, String email, LocalDate date) {
        ensureTicketIdAvailable(id);
        if (!userDirectory.exists(email)) {
            throw new ParkException("User " + email + " does not exist");
        }
        ensureNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, email, date, currentPrice);
        registerTicket(ticket);
        return ticket;
    }

    /**
     * Deletes a standalone individual ticket dated strictly after the current system date.
     *
     * @throws ParkException otherwise
     */
    public synchronized void deleteTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new ParkException("Individual ticket " + id + " does not exist");
        }
        if (individual.getContainingGroupTicket() != null) {
            throw new ParkException("Ticket " + id + " belongs to group ticket "
                    + individual.getContainingGroupTicket().getId() + " and cannot be deleted on its own");
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new ParkException("Ticket " + id + " is for today or a past date and must be kept");
        }
        tickets.remove(id);
    }

    /** @return whether the user has any ticket dated today or later (derived, never stored) */
    public synchronized boolean userHasActiveTicket(String email) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.concernsUser(email) && ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Guard for user deletion.
     *
     * @throws ParkException if the user has an active ticket
     */
    public synchronized void ensureUserDeletable(String email) {
        if (userHasActiveTicket(email)) {
            throw new ParkException("User " + email + " has active tickets and cannot be deleted");
        }
        for (Group group : groups.values()) {
            if (group.hasMember(email) && isGroupLocked(group.getId())) {
                throw new ParkException("User " + email + " belongs to group " + group.getId()
                        + ", which has an active group ticket");
            }
        }
    }

    /** Removes the user from every group, as an internal consequence of an allowed user deletion. */
    public synchronized void removeUserFromAllGroups(String email) {
        ensureUserDeletable(email);
        for (Group group : groups.values()) {
            group.removeMember(email);
        }
    }

    // ----- group tickets -----

    /**
     * Creates a group ticket for all current members of a non-empty group, all-or-nothing.
     *
     * @throws ParkException if the group is missing or empty, the date is past, or any identifier is used
     */
    public synchronized GroupTicket createGroupTicket(String id, String groupId, LocalDate date) {
        ensureTicketIdAvailable(id);
        Group group = groups.get(groupId);
        if (group == null) {
            throw new ParkException("Group " + groupId + " does not exist");
        }
        if (group.getMembers().isEmpty()) {
            throw new ParkException("Group " + groupId + " is empty");
        }
        ensureNotPast(date);
        int members = group.getMembers().size();
        BigDecimal price = GroupTicket.memberPrice(currentPrice, members);
        List<IndividualTicket> components = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        ids.add(id);
        for (String email : group.getMembers()) {
            String dni = userDirectory.dniOf(email);
            if (dni == null || dni.isEmpty()) {
                throw new ParkException("DNI of user " + email + " is unknown");
            }
            String componentId = id + ":" + dni;
            ensureTicketIdAvailable(componentId);
            if (!ids.add(componentId)) {
                throw new ParkException("Duplicate ticket identifier " + componentId);
            }
            components.add(new IndividualTicket(componentId, email, date, price));
        }
        GroupTicket groupTicket = new GroupTicket(id, group, date, components);
        tickets.put(id, groupTicket);
        for (IndividualTicket component : components) {
            tickets.put(component.getId(), component);
        }
        return groupTicket;
    }

    /**
     * Deletes a group ticket dated strictly after the current system date, with its composition.
     *
     * @throws ParkException otherwise
     */
    public synchronized void deleteGroupTicket(String id) {
        if (!(tickets.get(id) instanceof GroupTicket groupTicket)) {
            throw new ParkException("Group ticket " + id + " does not exist");
        }
        if (!groupTicket.isDeletableOn(currentDate)) {
            throw new ParkException("Group ticket " + id + " is for today or a past date and must be kept");
        }
        for (IndividualTicket component : groupTicket.getComponents()) {
            tickets.remove(component.getId());
        }
        tickets.remove(id);
    }

    // ----- attractions -----

    /**
     * Creates an attraction with no occupants.
     *
     * @throws ParkException if a field is empty, the capacity is not positive or the identifier is used
     */
    public synchronized Attraction createAttraction(String id, String name, int capacity) {
        if (isBlank(id) || isBlank(name)) {
            throw new ParkException("Attraction identifier and name are required");
        }
        if (capacity <= 0) {
            throw new ParkException("Capacity must be a positive whole number");
        }
        if (attractions.containsKey(id)) {
            throw new ParkException("Attraction " + id + " already exists");
        }
        Attraction attraction = new Attraction(id, name, capacity);
        attractions.put(id, attraction);
        return attraction;
    }

    public synchronized Attraction findAttraction(String id) {
        return attractions.get(id);
    }

    public synchronized Collection<Attraction> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    /**
     * Admits every user of a ticket valid on the current system date into an attraction, all or none.
     *
     * @return e-mails of the admitted users
     * @throws ParkException if the attraction or ticket is unknown, the ticket is not valid today, a user is
     *         already inside or capacity is insufficient for all of them
     */
    public synchronized List<String> enterAttraction(String attractionId, String ticketId) {
        Attraction attraction = attractions.get(attractionId);
        if (attraction == null) {
            throw new ParkException("Attraction " + attractionId + " does not exist");
        }
        Ticket ticket = tickets.get(ticketId);
        if (ticket == null) {
            throw new ParkException("Ticket " + ticketId + " does not exist");
        }
        if (!ticket.getDate().equals(currentDate)) {
            throw new ParkException("Ticket " + ticketId + " is not valid on " + currentDate);
        }
        List<String> holders = ticket.holders();
        for (String email : holders) {
            if (attraction.contains(email)) {
                throw new ParkException("User " + email + " is already inside attraction " + attractionId);
            }
        }
        if (attraction.getCapacity() - attraction.getOccupants().size() < holders.size()) {
            throw new ParkException("Attraction " + attractionId + " does not have capacity for "
                    + holders.size() + " user(s)");
        }
        for (String email : holders) {
            attraction.enter(email);
        }
        return holders;
    }

    /**
     * Removes a user from an attraction, releasing their place.
     *
     * @throws ParkException if the attraction is unknown or the user is not inside
     */
    public synchronized void leaveAttraction(String attractionId, String email) {
        Attraction attraction = attractions.get(attractionId);
        if (attraction == null) {
            throw new ParkException("Attraction " + attractionId + " does not exist");
        }
        if (!attraction.leave(email)) {
            throw new ParkException("User " + email + " is not inside attraction " + attractionId);
        }
    }

    // ----- restoring persisted state (used only by the persistence layer) -----

    /**
     * Rebuilds a persisted group with its members, without business-rule checks on members.
     *
     * @throws ParkException if the identifier is already used
     */
    public synchronized Group restoreGroup(String id, String name, List<String> members) {
        Group group = createGroup(id, name);
        for (String email : members) {
            group.addMember(email);
        }
        return group;
    }

    /**
     * Rebuilds a persisted standalone individual ticket (past dates and unknown users are valid history).
     *
     * @throws ParkException if the identifier is already used
     */
    public synchronized IndividualTicket restoreIndividualTicket(String id, String email, LocalDate date,
            BigDecimal price) {
        IndividualTicket ticket = new IndividualTicket(id, email, date, price);
        registerTicket(ticket);
        return ticket;
    }

    /**
     * Rebuilds a persisted group ticket and its components. The group link is restored only when
     * {@code linked} is true and the group exists.
     *
     * @throws ParkException if any identifier is already used
     */
    public synchronized GroupTicket restoreGroupTicket(String id, String groupId, boolean linked, LocalDate date,
            List<IndividualTicket> components) {
        ensureTicketIdAvailable(id);
        Set<String> ids = new HashSet<>();
        for (IndividualTicket component : components) {
            ensureTicketIdAvailable(component.getId());
            if (!ids.add(component.getId()) || component.getId().equals(id)) {
                throw new ParkException("Duplicate ticket identifier " + component.getId());
            }
        }
        Group group = linked ? groups.get(groupId) : null;
        GroupTicket ticket = new GroupTicket(id, groupId, group, date, components);
        tickets.put(id, ticket);
        for (IndividualTicket component : components) {
            tickets.put(component.getId(), component);
        }
        return ticket;
    }

    // ----- invoices (director of the invoice builders) -----

    /**
     * Builds the invoice of an existing user from the tickets bought directly for them.
     *
     * @throws ParkException if the user does not exist
     */
    public synchronized Invoice createUserInvoice(String email) {
        if (!userDirectory.exists(email)) {
            throw new ParkException("User " + email + " does not exist");
        }
        UserInvoiceBuilder builder = new UserInvoiceBuilder().forUser(email, userDirectory.dniOf(email));
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof IndividualTicket single && single.getContainingGroupTicket() == null
                    && single.getUserEmail().equals(email)) {
                builder.addIndividualTicket(single);
            }
        }
        return builder.build();
    }

    /**
     * Builds the invoice of an existing group from the group tickets it bought.
     *
     * @throws ParkException if the group does not exist
     */
    public synchronized Invoice createGroupInvoice(String groupId) {
        Group group = groups.get(groupId);
        if (group == null) {
            throw new ParkException("Group " + groupId + " does not exist");
        }
        GroupInvoiceBuilder builder = new GroupInvoiceBuilder().forGroup(group);
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                builder.addGroupTicket(groupTicket);
            }
        }
        return builder.build();
    }
}
