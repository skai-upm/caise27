package es.upm.etsisi.amusementpark.business.controller;

import es.upm.etsisi.amusementpark.business.exception.AlreadyExistsException;
import es.upm.etsisi.amusementpark.business.exception.AuthenticationException;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.etsisi.amusementpark.business.exception.ParkException;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.IndividualTicket;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import es.upm.etsisi.amusementpark.business.repository.ParkRepositories;
import es.upm.etsisi.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordHasher;
import es.upm.etsisi.amusementpark.business.security.PasswordPolicy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Single central element maintaining the overall park state during an execution.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private LocalDate currentDate;
    private BigDecimal currentPrice;
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private ParkRepositories repositories;
    private PasswordHasher passwordHasher = new BCryptPasswordHasher();

    private ParkController() {
        reset();
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Restores the start-of-execution state: local date, initial price, no elements. */
    public void reset() {
        currentDate = LocalDate.now();
        currentPrice = INITIAL_PRICE;
        users.clear();
        groups.clear();
        tickets.clear();
        attractions.clear();
    }

    /**
     * Injects the repositories used to load and save the park state. Called once
     * during application initialization; different configurations (normal or
     * temporary persistence, other implementations) need no change here.
     */
    public void configure(ParkRepositories repositories) {
        this.repositories = repositories;
    }

    /** Injects the password protection mechanism (BCrypt by default). */
    public void configure(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    /**
     * Validates a new password with the Delivery 1 rule, protects it and stores
     * only the protected value; changing it generates a new protected value.
     */
    public void setUserPassword(String email, String plainPassword) {
        User user = findUser(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        PasswordPolicy.validate(plainPassword);
        user.setPasswordHash(passwordHasher.hash(plainPassword));
    }

    /**
     * Verifies a user's password against the stored protected value.
     *
     * @return the authenticated user
     * @throws AuthenticationException if the user is unknown, has no password or the password does not match
     */
    public User authenticate(String email, String plainPassword) {
        User user = findUser(email).orElse(null);
        if (user == null || !user.hasPassword() || !passwordHasher.matches(plainPassword, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid e-mail or password");
        }
        return user;
    }

    /**
     * Loads the stored park state through the injected repositories.
     *
     * @return problems found; the affected records are skipped
     */
    public List<String> loadState() {
        List<String> problems = new ArrayList<>();
        if (repositories == null) {
            return problems;
        }
        for (User user : repositories.users().load()) {
            restore(problems, () -> addUser(user));
        }
        for (Group group : repositories.groups().load(this::findUser)) {
            restore(problems, () -> {
                Group created = createGroup(group.getIdentifier(), group.getName());
                group.getMembers().forEach(created::addMember);
            });
        }
        List<User> historicalUsers = new ArrayList<>();
        for (Ticket ticket : repositories.tickets().load(this::findGroup,
                (dni, email) -> resolveStoredUser(historicalUsers, dni, email))) {
            restore(problems, () -> registerTicket(ticket));
        }
        for (Attraction attraction : repositories.attractions().load()) {
            restore(problems, () -> restoreAttraction(attraction));
        }
        problems.addAll(repositories.users().getProblems());
        problems.addAll(repositories.groups().getProblems());
        problems.addAll(repositories.tickets().getProblems());
        problems.addAll(repositories.attractions().getProblems());
        return problems;
    }

    /** Saves the park state through the injected repositories (nothing when none are configured). */
    public void saveState() {
        if (repositories == null) {
            return;
        }
        repositories.users().save(getUsers());
        repositories.groups().save(getGroups());
        repositories.tickets().save(getTickets());
        repositories.attractions().save(getAttractions());
    }

    /** The active user with that DNI and e-mail, otherwise one shared historical user record. */
    private User resolveStoredUser(List<User> historical, String dni, String email) {
        Optional<User> active = findUser(email).filter(user -> user.getDni().equals(dni));
        if (active.isPresent()) {
            return active.get();
        }
        for (User user : historical) {
            if (user.getDni().equals(dni) && user.getEmail().equals(email)) {
                return user;
            }
        }
        User user = new User(dni, email);
        historical.add(user);
        return user;
    }

    private static void restore(List<String> problems, Runnable action) {
        try {
            action.run();
        } catch (ParkException e) {
            problems.add("Stored data ignored: " + e.getMessage());
        }
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    /** Top-level tickets (individual tickets and group tickets). */
    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    public Collection<Attraction> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    private void restoreAttraction(Attraction attraction) {
        if (attractions.containsKey(attraction.getIdentifier())) {
            throw new AlreadyExistsException("Attraction already exists: " + attraction.getIdentifier());
        }
        attractions.put(attraction.getIdentifier(), attraction);
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    /**
     * Replaces the current system date.
     *
     * @throws InvalidValueException if the text is not a valid {@code yyyy-MM-dd} date
     */
    public void setCurrentDate(String text) {
        currentDate = parseDate(text);
    }

    /**
     * Replaces the current base ticket price.
     *
     * @throws InvalidValueException if the text is not a valid, positive monetary amount
     */
    public void setCurrentPrice(String text) {
        currentPrice = parsePrice(text);
    }

    /**
     * @throws AlreadyExistsException if a user with the same e-mail already exists
     */
    public void addUser(User user) {
        if (users.containsKey(user.getEmail())) {
            throw new AlreadyExistsException("User already exists: " + user.getEmail());
        }
        users.put(user.getEmail(), user);
    }

    public Optional<User> findUser(String email) {
        return Optional.ofNullable(users.get(email));
    }

    /**
     * Creates an empty group.
     *
     * @throws AlreadyExistsException if the identifier is already in use
     */
    public Group createGroup(String identifier, String name) {
        if (groups.containsKey(identifier)) {
            throw new AlreadyExistsException("Group already exists: " + identifier);
        }
        Group group = new Group(identifier, name);
        groups.put(identifier, group);
        return group;
    }

    public Optional<Group> findGroup(String identifier) {
        return Optional.ofNullable(groups.get(identifier));
    }

    /**
     * A group is modifiable when it has no associated group ticket dated on or
     * after the current system date. The state is derived, never stored.
     */
    public boolean isGroupModifiable(Group group) {
        return findGroupTickets(group).stream().noneMatch(ticket -> ticket.isActive(currentDate));
    }

    /** Group tickets purchased by {@code group}, found by traversing the tickets. */
    public List<GroupTicket> findGroupTickets(Group group) {
        return tickets.values().stream()
                .filter(GroupTicket.class::isInstance)
                .map(GroupTicket.class::cast)
                .filter(ticket -> ticket.getGroup() == group)
                .toList();
    }

    /**
     * Deletes a group without active group tickets. Its historical group tickets
     * are kept, with their group reference cleared.
     */
    public void deleteGroup(String identifier) {
        Group group = modifiableGroup(identifier);
        findGroupTickets(group).forEach(GroupTicket::detachGroup);
        groups.remove(identifier);
    }

    public void addUserToGroup(String identifier, String email) {
        Group group = modifiableGroup(identifier);
        User user = findUser(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        group.addMember(user);
    }

    public void removeUserFromGroup(String identifier, String email) {
        modifiableGroup(identifier).removeMember(email);
    }

    private Group modifiableGroup(String identifier) {
        Group group = findGroup(identifier)
                .orElseThrow(() -> new NotFoundException("Group not found: " + identifier));
        if (!isGroupModifiable(group)) {
            throw new OperationNotAllowedException("Group " + identifier + " has an active group ticket and cannot be modified");
        }
        return group;
    }

    /**
     * Deletes a user. Rejected while the user has an active ticket or belongs to a
     * group that cannot be modified; otherwise the user is also removed from all
     * their groups. Historical tickets keep referencing the user.
     */
    public void deleteUser(String email) {
        User user = findUser(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        if (hasActiveTickets(user)) {
            throw new OperationNotAllowedException("User " + email + " has active tickets and cannot be deleted");
        }
        List<Group> memberships = groups.values().stream().filter(group -> group.hasMember(email)).toList();
        for (Group group : memberships) {
            if (!isGroupModifiable(group)) {
                throw new OperationNotAllowedException("User " + email + " belongs to group " + group.getIdentifier()
                        + " which has an active group ticket");
            }
        }
        memberships.forEach(group -> group.removeMember(email));
        users.remove(email);
    }

    // ---- tickets ----

    /** Identifier lookup over the shared space of all tickets, including composed ones. */
    public boolean isTicketIdentifierInUse(String identifier) {
        return findAnyTicket(identifier).isPresent();
    }

    public Optional<Ticket> findAnyTicket(String identifier) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.getIdentifier().equals(identifier)) {
                return Optional.of(ticket);
            }
            for (IndividualTicket part : ticket.getIndividualTickets()) {
                if (part.getIdentifier().equals(identifier)) {
                    return Optional.of(part);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Ticket> findTicket(String identifier) {
        return Optional.ofNullable(tickets.get(identifier));
    }

    /**
     * Registers a ticket after checking, in a common manner for every ticket type,
     * that neither its identifier nor those of its individual tickets are in use.
     * Nothing is registered if any identifier conflicts.
     */
    void registerTicket(Ticket ticket) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(ticket.getIdentifier());
        for (IndividualTicket part : ticket.getIndividualTickets()) {
            if (part != ticket) {
                identifiers.add(part.getIdentifier());
            }
        }
        for (String identifier : identifiers) {
            if (isTicketIdentifierInUse(identifier) || identifiers.indexOf(identifier) != identifiers.lastIndexOf(identifier)) {
                throw new AlreadyExistsException("Ticket identifier already in use: " + identifier);
            }
        }
        tickets.put(ticket.getIdentifier(), ticket);
    }

    /**
     * Creates an individual ticket at the current base price.
     */
    public IndividualTicket createIndividualTicket(String identifier, String email, String dateText) {
        LocalDate date = parseDate(dateText);
        User user = findUser(email).orElseThrow(() -> new NotFoundException("User not found: " + email));
        if (date.isBefore(currentDate)) {
            throw new OperationNotAllowedException("Tickets cannot be created for past dates: " + date);
        }
        IndividualTicket ticket = new IndividualTicket(identifier, date, user, currentPrice);
        registerTicket(ticket);
        return ticket;
    }

    /**
     * Deletes an individual ticket dated strictly after the current date.
     */
    public void deleteIndividualTicket(String identifier) {
        Ticket ticket = tickets.get(identifier);
        if (!(ticket instanceof IndividualTicket)) {
            if (isTicketIdentifierInUse(identifier)) {
                throw new OperationNotAllowedException("Ticket " + identifier
                        + " is not an individual ticket or belongs to a group ticket and cannot be deleted directly");
            }
            throw new NotFoundException("Ticket not found: " + identifier);
        }
        if (!ticket.isDeletable(currentDate)) {
            throw new OperationNotAllowedException("Ticket " + identifier
                    + " is for the current day or an earlier date and cannot be deleted");
        }
        tickets.remove(identifier);
    }

    /**
     * Creates a group ticket for the current members of a non-empty group, with the
     * member-count discount applied to the current base price.
     */
    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, String dateText) {
        LocalDate date = parseDate(dateText);
        Group group = findGroup(groupIdentifier)
                .orElseThrow(() -> new NotFoundException("Group not found: " + groupIdentifier));
        if (group.getMembers().isEmpty()) {
            throw new OperationNotAllowedException("Group " + groupIdentifier + " is empty");
        }
        if (date.isBefore(currentDate)) {
            throw new OperationNotAllowedException("Tickets cannot be created for past dates: " + date);
        }
        GroupTicket ticket = GroupTicket.purchase(identifier, date, group, currentPrice);
        registerTicket(ticket);
        return ticket;
    }

    /**
     * Deletes a group ticket dated strictly after the current date, together with its composition.
     */
    public void deleteGroupTicket(String identifier) {
        Ticket ticket = tickets.get(identifier);
        if (!(ticket instanceof GroupTicket)) {
            throw new NotFoundException("Group ticket not found: " + identifier);
        }
        if (!ticket.isDeletable(currentDate)) {
            throw new OperationNotAllowedException("Group ticket " + identifier
                    + " is for the current day or an earlier date and cannot be deleted");
        }
        tickets.remove(identifier);
    }

    /** The individual tickets (standalone or composed) of a user, found by traversal. */
    public List<IndividualTicket> findTicketsOfUser(User user) {
        return tickets.values().stream()
                .flatMap(ticket -> ticket.getIndividualTickets().stream())
                .filter(part -> part.getUser() == user)
                .toList();
    }

    /** Derived lock state of a user: any associated ticket that is still active. */
    public boolean hasActiveTickets(User user) {
        return findTicketsOfUser(user).stream().anyMatch(ticket -> ticket.isActive(currentDate));
    }

    // ---- attractions ----

    /**
     * Creates an attraction with no occupants.
     *
     * @throws AlreadyExistsException for a duplicate identifier; {@link InvalidValueException} for a capacity that is not a positive integer
     */
    public Attraction createAttraction(String identifier, String name, String capacityText) {
        int capacity = parseCapacity(capacityText);
        if (attractions.containsKey(identifier)) {
            throw new AlreadyExistsException("Attraction already exists: " + identifier);
        }
        Attraction attraction = new Attraction(identifier, name, capacity);
        attractions.put(identifier, attraction);
        return attraction;
    }

    public Optional<Attraction> findAttraction(String identifier) {
        return Optional.ofNullable(attractions.get(identifier));
    }

    /**
     * Lets the users of a ticket valid for the current date enter an attraction.
     * Works on the common ticket abstraction: the users admitted are those of the
     * ticket's individual tickets, and either all of them enter or none does.
     *
     * @return the number of users that entered
     */
    public int enterAttraction(String attractionIdentifier, String ticketIdentifier) {
        Attraction attraction = findAttraction(attractionIdentifier)
                .orElseThrow(() -> new NotFoundException("Attraction not found: " + attractionIdentifier));
        Ticket ticket = findTicket(ticketIdentifier)
                .orElseThrow(() -> new NotFoundException("Ticket not found: " + ticketIdentifier));
        if (!ticket.getDate().equals(currentDate)) {
            throw new OperationNotAllowedException("Ticket " + ticketIdentifier + " is valid for " + ticket.getDate()
                    + ", not for the current date " + currentDate);
        }
        List<User> entrants = ticket.getIndividualTickets().stream().map(IndividualTicket::getUser).toList();
        attraction.admit(entrants);
        return entrants.size();
    }

    /**
     * Removes the user with {@code email} from the attraction, releasing one place.
     */
    public void exitAttraction(String attractionIdentifier, String email) {
        Attraction attraction = findAttraction(attractionIdentifier)
                .orElseThrow(() -> new NotFoundException("Attraction not found: " + attractionIdentifier));
        User user = attraction.findOccupant(email).orElseThrow(() -> new NotFoundException(
                "User " + email + " is not inside attraction " + attractionIdentifier));
        attraction.removeOccupant(user);
    }

    private static int parseCapacity(String text) {
        if (text == null || text.isEmpty() || !isDigits(text)) {
            throw new InvalidValueException("Invalid capacity, expected a positive integer: " + text);
        }
        try {
            int capacity = Integer.parseInt(text);
            if (capacity <= 0) {
                throw new InvalidValueException("Invalid capacity, expected a positive integer: " + text);
            }
            return capacity;
        } catch (NumberFormatException e) {
            throw new InvalidValueException("Invalid capacity, expected a positive integer: " + text);
        }
    }

    public static LocalDate parseDate(String text) {
        if (text == null || text.length() != 10) {
            throw new InvalidValueException("Invalid date, expected yyyy-MM-dd: " + text);
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new InvalidValueException("Invalid date, expected yyyy-MM-dd: " + text);
        }
    }

    public static BigDecimal parsePrice(String text) {
        if (text == null || text.isEmpty()) {
            throw new InvalidValueException("Invalid price: " + text);
        }
        int dot = text.indexOf('.');
        String integerPart = dot < 0 ? text : text.substring(0, dot);
        String decimals = dot < 0 ? "" : text.substring(dot + 1);
        if (integerPart.isEmpty() || !isDigits(integerPart) || (dot >= 0 && (decimals.isEmpty()
                || decimals.length() > 2 || !isDigits(decimals)))) {
            throw new InvalidValueException("Invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(2);
        if (price.signum() <= 0) {
            throw new InvalidValueException("Invalid price, it must be positive: " + text);
        }
        return price;
    }

    private static boolean isDigits(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (!Character.isDigit(text.charAt(i)) || text.charAt(i) > '9') {
                return false;
            }
        }
        return true;
    }
}
