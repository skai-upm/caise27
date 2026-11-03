package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.InvalidDataException;
import es.upm.etsisi.amusementpark.business.NotFoundException;
import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.PasswordHasher;
import es.upm.etsisi.amusementpark.business.RuleViolationException;
import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.UserField;
import es.upm.etsisi.amusementpark.business.UserValidator;
import es.upm.etsisi.amusementpark.business.Values;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.attraction.EntryResult;
import es.upm.etsisi.amusementpark.business.attraction.PassLookup;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.repository.ParkRepositories;
import es.upm.etsisi.amusementpark.business.ticket.GroupTicket;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Pass;
import es.upm.etsisi.amusementpark.business.ticket.SpecialPass;
import es.upm.etsisi.amusementpark.business.ticket.SpeedPass;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Central park management. A single instance exists per execution; it owns the
 * users, groups, tickets, passes and attractions, the current system date and
 * the current base ticket price. Its repositories and password hasher are
 * injected at start-up.
 *
 * <p>Every operation validates first, then applies its change and saves all
 * repositories. If saving fails, the change is undone so that a rejected or
 * failed operation never leaves partial state.
 */
public final class ParkController implements PassLookup {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();
    private static final String NO_USER = "no user with email ";
    private static final String NO_TICKET = "no ticket with identifier ";
    private static final String SPEED_OPTION = "SPEED";

    private ParkRepositories repositories;
    private PasswordHasher passwordHasher;
    private List<User> users = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();
    private List<Pass> passes = new ArrayList<>();
    private List<Attraction> attractions = new ArrayList<>();
    private LocalDate today = LocalDate.now();
    private BigDecimal basePrice = INITIAL_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /**
     * Starts a new execution with the given dependencies: loads persisted data
     * and resets the date and price.
     */
    public void initialize(ParkRepositories parkRepositories, PasswordHasher hasher, LocalDate startDate)
            throws StorageException {
        List<User> loadedUsers = parkRepositories.users().load();
        Map<String, User> usersByDni = new HashMap<>();
        loadedUsers.forEach(user -> usersByDni.put(user.getDni(), user));
        List<Group> loadedGroups = parkRepositories.groups().load(usersByDni);
        Map<String, Group> groupsById = new HashMap<>();
        loadedGroups.forEach(group -> groupsById.put(group.getIdentifier(), group));
        List<Ticket> loadedTickets = parkRepositories.tickets().load(usersByDni, groupsById);
        Map<String, IndividualTicket> individualTickets = new HashMap<>();
        for (Ticket ticket : loadedTickets) {
            ticket.getEntryTickets().forEach(entry -> individualTickets.put(entry.getIdentifier(), entry));
        }
        List<Pass> loadedPasses = parkRepositories.passes().load(individualTickets);
        List<Attraction> loadedAttractions = parkRepositories.attractions().load();

        this.repositories = parkRepositories;
        this.passwordHasher = hasher;
        this.users = new ArrayList<>(loadedUsers);
        this.groups = new ArrayList<>(loadedGroups);
        this.tickets = new ArrayList<>(loadedTickets);
        this.passes = new ArrayList<>(loadedPasses);
        this.attractions = new ArrayList<>(loadedAttractions);
        this.today = startDate;
        this.basePrice = INITIAL_PRICE;
    }

    // ---------------------------------------------------------------- system

    public LocalDate getToday() {
        return today;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setDate(String value) throws InvalidDataException {
        today = requireDate(value);
    }

    public void setPrice(String value) throws InvalidDataException {
        BigDecimal price = Values.parsePrice(value);
        if (price == null) {
            throw new InvalidDataException("invalid price, expected a positive amount with at most two decimals");
        }
        basePrice = price;
    }

    // ----------------------------------------------------------------- users

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public void createUser(String firstName, String surnames, String dni, String email,
                           String password, String phone, String card) throws ParkException {
        if (!UserValidator.isValidDni(dni)) {
            throw new InvalidDataException("invalid DNI");
        }
        validateEmail(email);
        validatePassword(password);
        validateCard(card);
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                throw new RuleViolationException("a user with DNI " + dni + " already exists");
            }
        }
        if (findUser(email) != null) {
            throw new RuleViolationException("a user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, passwordHasher.hash(password), phone, card);
        users.add(user);
        commit(() -> users.remove(user));
    }

    public void deleteUser(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new RuleViolationException("the user has active tickets");
        }
        Map<Group, Integer> memberships = new LinkedHashMap<>();
        for (Group group : groups) {
            if (group.hasMember(user)) {
                if (isLocked(group)) {
                    throw new RuleViolationException("the user belongs to group " + group.getIdentifier()
                            + ", which has an active group ticket");
                }
                memberships.put(group, group.indexOf(user));
            }
        }
        int index = users.indexOf(user);
        users.remove(user);
        memberships.keySet().forEach(group -> group.removeMember(user));
        commit(() -> {
            users.add(index, user);
            memberships.forEach((group, position) -> group.addMember(position, user));
        });
        attractions.forEach(attraction -> attraction.release(user));
    }

    public void updateUser(String email, String password, String fieldName, String newValue) throws ParkException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new InvalidDataException("field " + fieldName + " cannot be modified");
        }
        String stored = newValue;
        switch (field) {
            case EMAIL -> {
                validateEmail(newValue);
                User owner = findUser(newValue);
                if (owner != null && owner != user) {
                    throw new RuleViolationException("a user with email " + newValue + " already exists");
                }
            }
            case PASSWORD -> {
                validatePassword(newValue);
                stored = passwordHasher.hash(newValue);
            }
            case CARD -> validateCard(newValue);
            default -> {
                // NAME, SURNAME and PHONE have no validation rules
            }
        }
        String oldValue = readField(user, field);
        writeField(user, field, stored);
        commit(() -> writeField(user, field, oldValue));
    }

    /** Returns the active user with the given email. */
    public User requireUser(String email) throws NotFoundException {
        User user = findUser(email);
        if (user == null) {
            throw new NotFoundException(NO_USER + email);
        }
        return user;
    }

    private User authenticate(String email, String password) throws ParkException {
        User user = requireUser(email);
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new RuleViolationException("incorrect password");
        }
        return user;
    }

    private User findUser(String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    private boolean hasActiveTicket(User user) {
        for (Ticket ticket : tickets) {
            if (ticket.isActiveOn(today) && ticket.getHolders().contains(user)) {
                return true;
            }
        }
        return false;
    }

    private static String readField(User user, UserField field) {
        return switch (field) {
            case NAME -> user.getFirstName();
            case SURNAME -> user.getSurnames();
            case EMAIL -> user.getEmail();
            case PASSWORD -> user.getPasswordHash();
            case PHONE -> user.getPhone();
            case CARD -> user.getCard();
        };
    }

    private static void writeField(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPasswordHash(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }

    private static void validateEmail(String email) throws InvalidDataException {
        if (!UserValidator.isValidEmail(email)) {
            throw new InvalidDataException("invalid email");
        }
    }

    private static void validatePassword(String password) throws InvalidDataException {
        if (!UserValidator.isValidPassword(password)) {
            throw new InvalidDataException("invalid password");
        }
    }

    private static void validateCard(String card) throws InvalidDataException {
        if (!UserValidator.isValidCard(card)) {
            throw new InvalidDataException("invalid card number");
        }
    }

    // ---------------------------------------------------------------- groups

    public List<Group> getGroups() {
        return List.copyOf(groups);
    }

    public void createGroup(String identifier, String name) throws ParkException {
        requireText(identifier, "group identifier");
        requireText(name, "group name");
        if (findGroup(identifier) != null) {
            throw new RuleViolationException("a group with identifier " + identifier + " already exists");
        }
        Group group = new Group(identifier, name);
        groups.add(group);
        commit(() -> groups.remove(group));
    }

    public void addToGroup(String identifier, String email) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        User user = requireUser(email);
        if (group.hasMember(user)) {
            throw new RuleViolationException("user " + email + " already belongs to group " + identifier);
        }
        group.addMember(user);
        commit(() -> group.removeMember(user));
    }

    public void removeFromGroup(String identifier, String email) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        User user = requireUser(email);
        if (!group.hasMember(user)) {
            throw new RuleViolationException("user " + email + " does not belong to group " + identifier);
        }
        int position = group.indexOf(user);
        group.removeMember(user);
        commit(() -> group.addMember(position, user));
    }

    public void deleteGroup(String identifier) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        List<GroupTicket> historical = groupTicketsOf(group);
        int index = groups.indexOf(group);
        groups.remove(group);
        historical.forEach(groupTicket -> groupTicket.setGroup(null));
        commit(() -> {
            groups.add(index, group);
            historical.forEach(groupTicket -> groupTicket.setGroup(group));
        });
    }

    /** A group is locked while it has a group ticket dated today or later. */
    public boolean isLocked(Group group) {
        for (GroupTicket groupTicket : groupTicketsOf(group)) {
            if (groupTicket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    public Group requireGroup(String identifier) throws NotFoundException {
        Group group = findGroup(identifier);
        if (group == null) {
            throw new NotFoundException("no group with identifier " + identifier);
        }
        return group;
    }

    /** Group tickets bought by the group, found by traversing the park's tickets. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    private Group requireModifiableGroup(String identifier) throws ParkException {
        Group group = requireGroup(identifier);
        if (isLocked(group)) {
            throw new RuleViolationException("group " + identifier + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    private Group findGroup(String identifier) {
        for (Group group : groups) {
            if (group.getIdentifier().equals(identifier)) {
                return group;
            }
        }
        return null;
    }

    // --------------------------------------------------------------- tickets

    public List<Ticket> getTickets() {
        return List.copyOf(tickets);
    }

    /** Finds any ticket in the shared identifier space, including composed tickets. */
    public Ticket findTicket(String identifier) {
        for (Ticket ticket : tickets) {
            if (ticket.getIdentifier().equals(identifier)) {
                return ticket;
            }
            if (ticket instanceof GroupTicket groupTicket) {
                for (IndividualTicket member : groupTicket.getComposition()) {
                    if (member.getIdentifier().equals(identifier)) {
                        return member;
                    }
                }
            }
        }
        return null;
    }

    /** Individual tickets bought directly for the user (not composing a group ticket). */
    public List<IndividualTicket> directTicketsOf(User user) {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof IndividualTicket individual && individual.getUser() == user) {
                result.add(individual);
            }
        }
        return result;
    }

    public IndividualTicket createTicket(String identifier, String email, String date) throws ParkException {
        requireText(identifier, "ticket identifier");
        User user = requireUser(email);
        LocalDate day = requireUsableDate(date);
        requireFreeIdentifier(identifier);
        IndividualTicket ticket = new IndividualTicket(identifier, day, user, basePrice, BigDecimal.ZERO);
        tickets.add(ticket);
        commit(() -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteTicket(String identifier) throws ParkException {
        Ticket ticket = findTicket(identifier);
        if (ticket == null) {
            throw new NotFoundException(NO_TICKET + identifier);
        }
        if (ticket instanceof GroupTicket) {
            throw new RuleViolationException("ticket " + identifier + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (!tickets.contains(ticket)) {
            throw new RuleViolationException("ticket " + identifier
                    + " belongs to a group ticket and cannot be deleted alone");
        }
        removeTicket(ticket);
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, String date) throws ParkException {
        requireText(identifier, "ticket identifier");
        Group group = requireGroup(groupIdentifier);
        LocalDate day = requireUsableDate(date);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new RuleViolationException("group " + groupIdentifier + " has no members");
        }
        requireFreeIdentifier(identifier);
        for (User member : members) {
            requireFreeIdentifier(GroupTicket.memberTicketIdentifier(identifier, member));
        }
        BigDecimal discount = GroupTicket.discountFor(members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(GroupTicket.memberTicketIdentifier(identifier, member), day, member,
                    basePrice, discount));
        }
        GroupTicket ticket = new GroupTicket(identifier, day, group, composition);
        tickets.add(ticket);
        commit(() -> tickets.remove(ticket));
        return ticket;
    }

    public void deleteGroupTicket(String identifier) throws ParkException {
        Ticket ticket = findTicket(identifier);
        if (!(ticket instanceof GroupTicket)) {
            throw new NotFoundException("no group ticket with identifier " + identifier);
        }
        removeTicket(ticket);
    }

    /** Deletes a future ticket together with every pass of its entry tickets. */
    private void removeTicket(Ticket ticket) throws ParkException {
        if (!ticket.isDeletableOn(today)) {
            throw new RuleViolationException("only tickets dated after " + today + " can be deleted");
        }
        List<Pass> previousPasses = new ArrayList<>(passes);
        passes.removeIf(pass -> ticket.getEntryTickets().contains(pass.getTicket()));
        int index = tickets.indexOf(ticket);
        tickets.remove(ticket);
        commit(() -> {
            tickets.add(index, ticket);
            passes = previousPasses;
        });
    }

    private IndividualTicket requireIndividualTicket(String identifier) throws ParkException {
        Ticket ticket = findTicket(identifier);
        if (ticket == null) {
            throw new NotFoundException(NO_TICKET + identifier);
        }
        if (!(ticket instanceof IndividualTicket individual)) {
            throw new RuleViolationException("ticket " + identifier + " is not an individual ticket");
        }
        if (!individual.isActiveOn(today)) {
            throw new RuleViolationException("passes cannot be bought for tickets dated before " + today);
        }
        return individual;
    }

    private LocalDate requireUsableDate(String value) throws ParkException {
        LocalDate day = requireDate(value);
        if (day.isBefore(today)) {
            throw new RuleViolationException("tickets cannot be created for past dates");
        }
        return day;
    }

    private static LocalDate requireDate(String value) throws InvalidDataException {
        LocalDate day = Values.parseDate(value);
        if (day == null) {
            throw new InvalidDataException("invalid date, expected yyyy-MM-dd");
        }
        return day;
    }

    private void requireFreeIdentifier(String identifier) throws RuleViolationException {
        if (findTicket(identifier) != null) {
            throw new RuleViolationException("a ticket with identifier " + identifier + " already exists");
        }
    }

    // ---------------------------------------------------------------- passes

    public List<Pass> getPasses() {
        return List.copyOf(passes);
    }

    /** Passes bought for any individual ticket held by the user, including composed tickets. */
    public List<Pass> passesOf(User user) {
        List<Pass> result = new ArrayList<>();
        for (Pass pass : passes) {
            if (pass.getTicket().getUser() == user) {
                result.add(pass);
            }
        }
        return result;
    }

    /** Buys 1 or 10 SpeedPasses identified {@code <purchase>:<n>}, all or none. */
    public List<SpeedPass> createSpeedPasses(String purchaseIdentifier, String ticketIdentifier, String quantity)
            throws ParkException {
        requireText(purchaseIdentifier, "purchase identifier");
        if (!"1".equals(quantity) && !"10".equals(quantity)) {
            throw new InvalidDataException("quantity must be 1 or 10");
        }
        IndividualTicket ticket = requireIndividualTicket(ticketIdentifier);
        List<SpeedPass> purchase = new ArrayList<>();
        int count = Integer.parseInt(quantity);
        for (int number = 1; number <= count; number++) {
            String identifier = purchaseIdentifier + ":" + number;
            requireFreePassIdentifier(identifier);
            purchase.add(SpeedPass.purchase(identifier, ticket));
        }
        passes.addAll(purchase);
        commit(() -> passes.removeAll(purchase));
        return purchase;
    }

    public SpecialPass createSpecialPass(String identifier, String ticketIdentifier) throws ParkException {
        requireText(identifier, "pass identifier");
        IndividualTicket ticket = requireIndividualTicket(ticketIdentifier);
        if (findSpecialPass(ticket).isPresent()) {
            throw new RuleViolationException("ticket " + ticketIdentifier + " already has a Special Pass");
        }
        requireFreePassIdentifier(identifier);
        SpecialPass pass = SpecialPass.purchase(identifier, ticket);
        passes.add(pass);
        commit(() -> passes.remove(pass));
        return pass;
    }

    /** Refunds an unconsumed pass of the given kind whose ticket is dated after today. */
    public void deletePass(String identifier, Class<? extends Pass> kind) throws ParkException {
        Pass pass = findPass(identifier);
        if (!kind.isInstance(pass)) {
            throw new NotFoundException("no " + kind.getSimpleName() + " with identifier " + identifier);
        }
        if (pass.isConsumed()) {
            throw new RuleViolationException("pass " + identifier + " has been consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(today)) {
            throw new RuleViolationException("only passes of tickets dated after " + today + " can be refunded");
        }
        int index = passes.indexOf(pass);
        passes.remove(pass);
        commit(() -> passes.add(index, pass));
    }

    @Override
    public Optional<SpeedPass> findUnusedSpeedPass(IndividualTicket ticket) {
        for (Pass pass : passes) {
            if (pass instanceof SpeedPass speedPass && pass.getTicket() == ticket && !pass.isConsumed()) {
                return Optional.of(speedPass);
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<SpecialPass> findSpecialPass(IndividualTicket ticket) {
        for (Pass pass : passes) {
            if (pass instanceof SpecialPass specialPass && pass.getTicket() == ticket) {
                return Optional.of(specialPass);
            }
        }
        return Optional.empty();
    }

    private Pass findPass(String identifier) {
        for (Pass pass : passes) {
            if (pass.getIdentifier().equals(identifier)) {
                return pass;
            }
        }
        return null;
    }

    private void requireFreePassIdentifier(String identifier) throws RuleViolationException {
        if (findPass(identifier) != null) {
            throw new RuleViolationException("a pass with identifier " + identifier + " already exists");
        }
    }

    // ----------------------------------------------------------- attractions

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions);
    }

    /** Creates an attraction; a missing type means GENERAL. */
    public Attraction createAttraction(String identifier, String name, String capacity, String typeName)
            throws ParkException {
        requireText(identifier, "attraction identifier");
        requireText(name, "attraction name");
        int places = Values.parseCapacity(capacity);
        if (places <= 0) {
            throw new InvalidDataException("capacity must be a positive integer");
        }
        AttractionType type = typeName == null ? AttractionType.GENERAL : AttractionType.fromName(typeName);
        if (type == null) {
            throw new InvalidDataException("unknown attraction type " + typeName
                    + "; expected GENERAL, INDIVIDUAL, GROUP or SPECIAL");
        }
        if (findAttraction(identifier) != null) {
            throw new RuleViolationException("an attraction with identifier " + identifier + " already exists");
        }
        Attraction attraction = type.create(identifier, name, places);
        attractions.add(attraction);
        commit(() -> attractions.remove(attraction));
        return attraction;
    }

    public Attraction requireAttraction(String identifier) throws NotFoundException {
        Attraction attraction = findAttraction(identifier);
        if (attraction == null) {
            throw new NotFoundException("no attraction with identifier " + identifier);
        }
        return attraction;
    }

    /**
     * Admits every holder of the ticket, or nobody. The ticket must be valid
     * today; the attraction checks the ticket kind, passes and capacity.
     *
     * @param option null for a normal entry or {@code SPEED}
     */
    public EntryResult enter(String attractionIdentifier, String ticketIdentifier, String option)
            throws ParkException {
        if (option != null && !SPEED_OPTION.equals(option)) {
            throw new InvalidDataException("unknown entry option " + option + "; expected SPEED");
        }
        Attraction attraction = requireAttraction(attractionIdentifier);
        Ticket ticket = findTicket(ticketIdentifier);
        if (ticket == null) {
            throw new NotFoundException(NO_TICKET + ticketIdentifier);
        }
        if (!ticket.isValidOn(today)) {
            throw new RuleViolationException("ticket " + ticketIdentifier + " is not valid on " + today);
        }
        for (User holder : ticket.getHolders()) {
            if (!users.contains(holder)) {
                throw new RuleViolationException("user " + holder.getEmail() + " is no longer registered");
            }
        }
        EntryResult result = attraction.enter(ticket, option != null, this);
        if (!result.consumedPasses().isEmpty()) {
            commit(() -> {
                result.admitted().forEach(attraction::release);
                result.consumedPasses().forEach(Pass::cancelConsumption);
            });
        }
        return result;
    }

    public void exit(String attractionIdentifier, String email) throws ParkException {
        Attraction attraction = requireAttraction(attractionIdentifier);
        for (User occupant : attraction.getOccupants()) {
            if (occupant.getEmail().equals(email)) {
                attraction.release(occupant);
                return;
            }
        }
        throw new RuleViolationException("user " + email + " is not inside " + attractionIdentifier);
    }

    private Attraction findAttraction(String identifier) {
        for (Attraction attraction : attractions) {
            if (attraction.getIdentifier().equals(identifier)) {
                return attraction;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- common

    private static void requireText(String value, String description) throws InvalidDataException {
        if (value.isBlank()) {
            throw new InvalidDataException(description + " must not be empty");
        }
    }

    private void commit(Runnable undo) throws StorageException {
        try {
            saveAll();
        } catch (StorageException e) {
            undo.run();
            restoreFiles();
            throw e;
        }
    }

    private void restoreFiles() {
        try {
            saveAll();
        } catch (StorageException e) {
            // The original failure is reported to the caller; memory is already consistent.
        }
    }

    private void saveAll() throws StorageException {
        repositories.users().save(users);
        repositories.groups().save(groups);
        repositories.tickets().save(tickets);
        repositories.attractions().save(attractions);
        repositories.passes().save(passes);
    }
}
