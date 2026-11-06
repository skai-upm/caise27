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
    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();
    private final UserDirectory registeredUsers = new UserDirectory() {
        @Override
        public boolean exists(String email) {
            return users.containsKey(email);
        }

        @Override
        public String dniOf(String email) {
            User user = users.get(email);
            return user == null ? null : user.getDni();
        }
    };
    private UserDirectory userDirectory = registeredUsers;
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
        passes.clear();
        users.clear();
        userDirectory = registeredUsers;
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
        removePassesOf(individual);
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
            removePassesOf(component);
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
    public synchronized Attraction<?> createAttraction(String id, String name, int capacity) {
        return createAttraction(id, name, capacity, AttractionType.GENERAL);
    }

    /**
     * Creates an attraction of the given type with no occupants.
     *
     * @throws ParkException if a field is empty, the capacity is not positive or the identifier is used
     */
    public synchronized Attraction<?> createAttraction(String id, String name, int capacity, AttractionType type) {
        if (isBlank(id) || isBlank(name)) {
            throw new ParkException("Attraction identifier and name are required");
        }
        if (capacity <= 0) {
            throw new ParkException("Capacity must be a positive whole number");
        }
        if (attractions.containsKey(id)) {
            throw new ParkException("Attraction " + id + " already exists");
        }
        Attraction<?> attraction = Objects.requireNonNull(type, "type").create(id, name, capacity);
        attractions.put(id, attraction);
        return attraction;
    }

    public synchronized Attraction<?> findAttraction(String id) {
        return attractions.get(id);
    }

    public synchronized Collection<Attraction<?>> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    /**
     * Admits every user of a ticket valid on the current system date into an attraction, all or none, without
     * SpeedPasses.
     *
     * @return e-mails of the admitted users
     */
    public synchronized List<String> enterAttraction(String attractionId, String ticketId) {
        return enterAttraction(attractionId, ticketId, false).holders();
    }

    /**
     * Two-phase entry: every condition (attraction and ticket existence, validity today, ticket kind, pass use,
     * users not inside, capacity) is checked first; only then are users admitted and passes consumed.
     *
     * @param speed whether SpeedPasses are used
     * @return the admitted users and the consumed passes
     * @throws ParkException if any condition fails (no state changes)
     */
    public synchronized Attraction.Admission enterAttraction(String attractionId, String ticketId, boolean speed) {
        Attraction<?> attraction = attractions.get(attractionId);
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
        Attraction.Admission admission = attraction.admit(ticket, this::passesOfTicket, speed);
        for (String email : admission.holders()) {
            if (attraction.contains(email)) {
                throw new ParkException("User " + email + " is already inside attraction " + attractionId);
            }
        }
        if (attraction.getAvailablePlaces() < admission.holders().size()) {
            throw new ParkException("Attraction " + attractionId + " does not have capacity for "
                    + admission.holders().size() + " user(s)");
        }
        for (String email : admission.holders()) {
            attraction.enter(email);
        }
        for (Pass pass : admission.toConsume()) {
            pass.markConsumed();
        }
        return admission;
    }

    /**
     * Removes a user from an attraction, releasing their place.
     *
     * @throws ParkException if the attraction is unknown or the user is not inside
     */
    public synchronized void leaveAttraction(String attractionId, String email) {
        Attraction<?> attraction = attractions.get(attractionId);
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
        for (Pass pass : passes.values()) {
            if (pass.getTicket().getUserEmail().equals(email)) {
                builder.addPass(pass.getTypeName() + " " + pass.getId() + " for " + pass.getTicket().getId(),
                        pass.getPrice());
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

    // ----- passes -----

    public synchronized Pass findPass(String id) {
        return passes.get(id);
    }

    public synchronized Collection<Pass> getPasses() {
        return Collections.unmodifiableCollection(passes.values());
    }

    /** @return the passes of the individual ticket with that identifier (empty if none) */
    public synchronized List<Pass> passesOfTicket(String ticketId) {
        return tickets.get(ticketId) instanceof IndividualTicket ticket ? passesOf(ticket) : List.of();
    }

    /** @return the passes associated with an individual ticket (traversal; tickets hold no passes) */
    public synchronized List<Pass> passesOf(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passes.values()) {
            if (pass.getTicket() == ticket) {
                result.add(pass);
            }
        }
        return result;
    }

    private IndividualTicket ticketForPass(String ticketId) {
        if (!(tickets.get(ticketId) instanceof IndividualTicket ticket)) {
            throw new ParkException("Individual ticket " + ticketId + " does not exist");
        }
        if (ticket.getDate().isBefore(currentDate)) {
            throw new ParkException("Ticket " + ticketId + " is for a past date");
        }
        return ticket;
    }

    private void ensurePassIdAvailable(String id) {
        if (isBlank(id)) {
            throw new ParkException("Pass identifier is required");
        }
        if (passes.containsKey(id)) {
            throw new ParkException("Pass " + id + " already exists");
        }
    }

    /**
     * Buys 1 or 10 SpeedPasses identified {@code <purchaseId>:<n>}, all-or-nothing.
     *
     * @return the created passes
     */
    public synchronized List<SpeedPass> buySpeedPasses(String purchaseId, String ticketId, int quantity) {
        if (quantity != 1 && quantity != 10) {
            throw new ParkException("Quantity must be 1 or 10");
        }
        if (isBlank(purchaseId)) {
            throw new ParkException("Purchase identifier is required");
        }
        IndividualTicket ticket = ticketForPass(ticketId);
        List<SpeedPass> created = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            ensurePassIdAvailable(purchaseId + ":" + n);
            created.add(new SpeedPass(purchaseId + ":" + n, ticket));
        }
        for (SpeedPass pass : created) {
            passes.put(pass.getId(), pass);
        }
        return created;
    }

    /** Buys the Special Pass of an individual ticket (at most one per ticket, consumed or not). */
    public synchronized SpecialPass buySpecialPass(String id, String ticketId) {
        ensurePassIdAvailable(id);
        IndividualTicket ticket = ticketForPass(ticketId);
        for (Pass pass : passesOf(ticket)) {
            if (pass instanceof SpecialPass) {
                throw new ParkException("Ticket " + ticketId + " already has Special Pass " + pass.getId());
            }
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        passes.put(id, pass);
        return pass;
    }

    /**
     * Refunds (deletes) an unconsumed pass of the given type whose ticket is dated strictly in the future.
     */
    public synchronized void refundPass(String id, Class<? extends Pass> type) {
        Pass pass = passes.get(id);
        if (!type.isInstance(pass)) {
            throw new ParkException(typeName(type) + " " + id + " does not exist");
        }
        if (pass.isConsumed()) {
            throw new ParkException("Pass " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.getTicket().isDeletableOn(currentDate)) {
            throw new ParkException("Pass " + id + " belongs to a ticket for today or a past date");
        }
        passes.remove(id);
    }

    private static String typeName(Class<? extends Pass> type) {
        return type == SpecialPass.class ? "Special Pass" : type == SpeedPass.class ? "SpeedPass" : "Pass";
    }

    /** Marks a pass as consumed; it stays in the system as a purchase record. */
    public synchronized void markPassConsumed(String id) {
        Pass pass = passes.get(id);
        if (pass == null) {
            throw new ParkException("Pass " + id + " does not exist");
        }
        pass.markConsumed();
    }

    private void removePassesOf(IndividualTicket ticket) {
        passes.values().removeIf(pass -> pass.getTicket() == ticket);
    }

    /**
     * Rebuilds a persisted pass (used only by the persistence layer).
     *
     * @throws ParkException if the identifier is used or the ticket is not an individual ticket
     */
    public synchronized Pass restorePass(String id, String ticketId, boolean special, boolean consumed) {
        return restorePass(id, ticketId, special, null, consumed);
    }

    /**
     * Rebuilds a persisted pass with its stored price ({@code null} derives it from the ticket, for files written
     * before prices were stored).
     *
     * @throws ParkException if the identifier is used or the ticket is not an individual ticket
     */
    public synchronized Pass restorePass(String id, String ticketId, boolean special, BigDecimal storedPrice,
            boolean consumed) {
        ensurePassIdAvailable(id);
        if (!(tickets.get(ticketId) instanceof IndividualTicket ticket)) {
            throw new ParkException("Individual ticket " + ticketId + " does not exist");
        }
        Pass pass;
        if (special) {
            pass = storedPrice == null ? new SpecialPass(id, ticket) : new SpecialPass(id, ticket, storedPrice);
        } else {
            pass = storedPrice == null ? new SpeedPass(id, ticket) : new SpeedPass(id, ticket, storedPrice);
        }
        if (consumed) {
            pass.markConsumed();
        }
        passes.put(id, pass);
        return pass;
    }

    // ----- users -----

    /**
     * Registers a new user: validates the data, checks DNI and e-mail uniqueness, protects the password and stores
     * the user.
     *
     * @throws ParkException if the data is invalid or the DNI or e-mail already exists
     */
    public synchronized User registerUser(String firstName, String surnames, String dni, String email,
            String password, String phone, String card) {
        if (isBlank(firstName) || isBlank(surnames) || isBlank(phone)) {
            throw new ParkException("First name, surnames and phone are required");
        }
        UserValidator.validate(dni, email, password, card);
        for (User user : users.values()) {
            if (user.getDni().equals(dni)) {
                throw new ParkException("User with DNI " + dni + " already exists");
            }
        }
        if (users.containsKey(email)) {
            throw new ParkException("User with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, protectPassword(password), phone, card);
        users.put(email, user);
        return user;
    }

    /**
     * Rebuilds a persisted user with its stored (already protected) password (used only by persistence).
     *
     * @throws ParkException if the DNI or e-mail is already used
     */
    public synchronized User restoreUser(String firstName, String surnames, String dni, String email,
            String storedPassword, String phone, String card) {
        for (User user : users.values()) {
            if (user.getDni().equals(dni)) {
                throw new ParkException("User with DNI " + dni + " already exists");
            }
        }
        if (users.containsKey(email)) {
            throw new ParkException("User with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, storedPassword, phone, card);
        users.put(email, user);
        return user;
    }

    /**
     * Deletes a user after verifying the password: the user is removed from every group and from the registry;
     * historical tickets are kept.
     *
     * @throws ParkException if the user does not exist, the password is wrong, or active tickets or locked groups
     *         forbid the deletion (nothing changes)
     */
    public synchronized void deleteUser(String email, String password) {
        User user = users.get(email);
        if (user == null) {
            throw new ParkException("User " + email + " does not exist");
        }
        if (!verifyPassword(password, user.getPassword())) {
            throw new ParkException("Incorrect password for user " + email);
        }
        ensureUserDeletable(email);
        removeUserFromAllGroups(email);
        users.remove(email);
    }

    public synchronized User findUser(String email) {
        return users.get(email);
    }

    public synchronized Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }
}
