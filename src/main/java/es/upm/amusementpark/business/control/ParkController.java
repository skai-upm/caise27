package es.upm.amusementpark.business.control;

import es.upm.amusementpark.business.exception.DuplicateElementException;
import es.upm.amusementpark.business.exception.ElementNotFoundException;
import es.upm.amusementpark.business.exception.InvalidValueException;
import es.upm.amusementpark.business.exception.OperationNotAllowedException;
import es.upm.amusementpark.business.model.AccessTicket;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.Entry;
import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Pass;
import es.upm.amusementpark.business.model.PassLookup;
import es.upm.amusementpark.business.model.SpecialPass;
import es.upm.amusementpark.business.model.SpeedPass;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import es.upm.amusementpark.business.repository.Repositories;
import es.upm.amusementpark.business.security.PasswordProtector;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Central park management. Only one instance exists per execution; it keeps
 * the current system date, the current base ticket price, and centralizes
 * access to the users, groups, tickets and attractions of the park.
 */
public final class ParkController {

    public static final BigDecimal DEFAULT_PRICE = new BigDecimal("40.00");

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private LocalDate currentDate;
    private BigDecimal basePrice;
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private Repositories repositories;
    private final PasswordProtector passwords = new PasswordProtector();

    private ParkController() {
        reset();
    }

    private static final class Holder {
        private static final ParkController INSTANCE = new ParkController();
    }

    public static ParkController getInstance() {
        return Holder.INSTANCE;
    }

    /** Restores the start-of-execution state and removes any repository configuration. */
    public void reset() {
        repositories = null;
        currentDate = LocalDate.now();
        basePrice = DEFAULT_PRICE;
        users.clear();
        groups.clear();
        tickets.clear();
        attractions.clear();
        passes.clear();
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    /**
     * Sets the current system date from a {@code yyyy-MM-dd} text.
     *
     * @throws InvalidValueException if the text is not a valid date in that format
     */
    public void setCurrentDate(String text) {
        currentDate = parseDate(text);
    }

    /**
     * Parses a {@code yyyy-MM-dd} date strictly.
     *
     * @throws InvalidValueException if the text is not a valid date in that format
     */
    public static LocalDate parseDate(String text) {
        try {
            return LocalDate.parse(text.trim(), DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidValueException("invalid date (expected yyyy-MM-dd): " + text, e);
        }
    }

    /**
     * Sets the current base ticket price.
     *
     * @throws InvalidValueException if the text is not a positive amount with at most two decimals
     */
    public void setBasePrice(String text) {
        basePrice = parsePrice(text.trim());
    }

    private static BigDecimal parsePrice(String text) {
        int digits = 0;
        int decimals = 0;
        boolean seenPoint = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '.' && !seenPoint) {
                seenPoint = true;
            } else if (c >= '0' && c <= '9') {
                digits++;
                if (seenPoint) {
                    decimals++;
                }
            } else {
                throw new InvalidValueException("invalid price: " + text);
            }
        }
        if (digits == 0 || decimals > 2 || text.startsWith(".") || text.endsWith(".")) {
            throw new InvalidValueException("invalid price: " + text);
        }
        BigDecimal price = new BigDecimal(text).setScale(2);
        if (price.signum() <= 0) {
            throw new InvalidValueException("price must be positive: " + text);
        }
        return price;
    }

    // --- users -----------------------------------------------------------

    /** Adds a user to the park's user directory. */
    public void registerUser(User user) {
        users.put(user.getEmail(), user);
    }

    /** @return the registered user with that email, or {@code null} */
    public User findUser(String email) {
        return users.get(email);
    }

    /** Sets or changes a user's password, storing a new BCrypt-protected value. */
    public void setPassword(String email, String plainPassword) {
        User user = existingUser(email);
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new InvalidValueException("password must not be empty");
        }
        user.setPasswordHash(passwords.protect(plainPassword));
    }

    /**
     * Verifies the entered password of a user against the stored protected value.
     *
     * @throws OperationNotAllowedException if the password does not match or none is stored
     */
    public User verifyPassword(String email, String plainPassword) {
        User user = existingUser(email);
        if (!passwords.matches(plainPassword, user.getPasswordHash())) {
            throw new OperationNotAllowedException("invalid password for user " + user.getEmail());
        }
        return user;
    }

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    // --- persistence through injected repositories ----------------------

    /** Injects the repositories configured at application initialization. */
    public void configure(Repositories configured) {
        this.repositories = configured;
    }

    /**
     * Rebuilds the park state from the configured repositories.
     *
     * @return stored entries that could not be read and were skipped
     */
    public List<String> load() {
        List<String> skipped = new ArrayList<>();
        if (repositories == null) {
            return skipped;
        }
        for (User user : repositories.users().loadAll(skipped)) {
            registerUser(user);
        }
        repositories.groups().loadInto(this, skipped);
        repositories.attractions().loadInto(this, skipped);
        repositories.tickets().loadInto(this, skipped);
        repositories.passes().loadInto(this, skipped);
        return skipped;
    }

    /** Saves the park state through the configured repositories, if any. */
    public void persist() {
        if (repositories == null) {
            return;
        }
        repositories.users().saveAll(getUsers());
        repositories.groups().saveAll(getGroups());
        repositories.attractions().saveAll(getAttractions());
        repositories.tickets().saveAll(getTickets());
        repositories.passes().saveAll(getPasses());
    }

    // --- reconstruction (used by the repositories) -----------------------

    /** The registered user with that email, or a detached user with the stored data. */
    public User resolveUser(String email, String dni) {
        User user = users.get(email);
        return user != null ? user : new User(email, dni);
    }

    public void restoreGroup(String identifier, String name) {
        createGroup(identifier, name);
    }

    public void restoreMember(String groupIdentifier, String email) {
        Group group = existingGroup(groupIdentifier);
        if (!group.addMember(existingUser(email))) {
            throw new DuplicateElementException("duplicate member " + email);
        }
    }

    /** Adds a stored pass, keeping pass identifier uniqueness but not date rules. */
    public void restorePass(Pass pass) {
        if (passes.containsKey(pass.getIdentifier())) {
            throw new DuplicateElementException("pass identifier already in use: " + pass.getIdentifier());
        }
        passes.put(pass.getIdentifier(), pass);
    }

    /** Adds a stored ticket, keeping identifier uniqueness but not date rules. */
    public void restoreTicket(Ticket ticket) {
        addTicket(ticket);
    }

    // --- groups ----------------------------------------------------------

    public Group findGroup(String identifier) {
        return groups.get(identifier);
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public Group createGroup(String identifier, String name) {
        String id = requireText(identifier, "group identifier");
        String groupName = requireText(name, "group name");
        if (groups.containsKey(id)) {
            throw new DuplicateElementException("group already exists: " + id);
        }
        Group group = new Group(id, groupName);
        groups.put(id, group);
        return group;
    }

    public void addUserToGroup(String identifier, String email) {
        Group group = existingGroup(identifier);
        User user = existingUser(email);
        requireModifiable(group);
        if (!group.addMember(user)) {
            throw new DuplicateElementException("user " + user.getEmail()
                    + " already belongs to group " + group.getIdentifier());
        }
    }

    public void removeUserFromGroup(String identifier, String email) {
        Group group = existingGroup(identifier);
        String address = requireText(email, "email");
        requireModifiable(group);
        User user = users.get(address);
        if (user == null || !group.removeMember(user)) {
            throw new OperationNotAllowedException("user " + address
                    + " does not belong to group " + group.getIdentifier());
        }
    }

    public void deleteGroup(String identifier) {
        Group group = existingGroup(identifier);
        if (hasActiveGroupTicket(group)) {
            throw new OperationNotAllowedException("group " + group.getIdentifier()
                    + " has a group ticket for the current date or later");
        }
        for (GroupTicket ticket : groupTicketsOf(group)) {
            ticket.releaseGroup();
        }
        groups.remove(group.getIdentifier());
    }

    // --- passes ----------------------------------------------------------

    public Pass findPass(String identifier) {
        return passes.get(identifier);
    }

    public Collection<Pass> getPasses() {
        return Collections.unmodifiableCollection(passes.values());
    }

    /** Passes associated with an individual ticket, found by traversing the park's passes. */
    public List<Pass> passesOf(IndividualTicket ticket) {
        return passes.values().stream().filter(p -> p.getTicket() == ticket).toList();
    }

    /** Individual ticket with that identifier, either top-level or part of a group ticket. */
    public IndividualTicket findIndividualTicket(String identifier) {
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof IndividualTicket individual && individual.getIdentifier().equals(identifier)) {
                return individual;
            }
            if (ticket instanceof GroupTicket group) {
                for (IndividualTicket component : group.getComposition()) {
                    if (component.getIdentifier().equals(identifier)) {
                        return component;
                    }
                }
            }
        }
        return null;
    }

    /** Buys 1 or 10 SpeedPasses named {@code <purchase>:<n>}; all or nothing. */
    public List<SpeedPass> buySpeedPasses(String purchaseIdentifier, String ticketIdentifier, String quantityText) {
        String purchase = requireText(purchaseIdentifier, "purchase identifier");
        IndividualTicket ticket = purchasableTicket(ticketIdentifier);
        String quantityValue = quantityText == null ? "" : quantityText.trim();
        if (!quantityValue.equals("1") && !quantityValue.equals("10")) {
            throw new InvalidValueException("quantity must be 1 or 10: " + quantityText);
        }
        int quantity = Integer.parseInt(quantityValue);
        List<SpeedPass> bought = new ArrayList<>();
        for (int n = 1; n <= quantity; n++) {
            String id = purchase + ":" + n;
            if (passes.containsKey(id)) {
                throw new DuplicateElementException("pass identifier already in use: " + id);
            }
            bought.add(new SpeedPass(id, ticket));
        }
        for (SpeedPass pass : bought) {
            passes.put(pass.getIdentifier(), pass);
        }
        return bought;
    }

    /** Buys the Special Pass of an individual ticket (at most one per ticket). */
    public SpecialPass buySpecialPass(String identifier, String ticketIdentifier) {
        String id = requireText(identifier, "pass identifier");
        IndividualTicket ticket = purchasableTicket(ticketIdentifier);
        if (passes.containsKey(id)) {
            throw new DuplicateElementException("pass identifier already in use: " + id);
        }
        for (Pass pass : passesOf(ticket)) {
            if (pass instanceof SpecialPass) {
                throw new DuplicateElementException("ticket " + ticket.getIdentifier()
                        + " already has special pass " + pass.getIdentifier());
            }
        }
        SpecialPass pass = new SpecialPass(id, ticket);
        passes.put(id, pass);
        return pass;
    }

    public void refundSpeedPass(String identifier) {
        refund(identifier, SpeedPass.class, "speed pass");
    }

    public void refundSpecialPass(String identifier) {
        refund(identifier, SpecialPass.class, "special pass");
    }

    private void refund(String identifier, Class<? extends Pass> type, String what) {
        String id = requireText(identifier, "pass identifier");
        Pass pass = passes.get(id);
        if (!type.isInstance(pass)) {
            throw new ElementNotFoundException(what + " not found: " + id);
        }
        if (pass.isConsumed()) {
            throw new OperationNotAllowedException(what + " " + id + " has been consumed and cannot be refunded");
        }
        if (!pass.isRefundableOn(currentDate)) {
            throw new OperationNotAllowedException(what + " " + id
                    + " belongs to a ticket for the current date or earlier and cannot be refunded");
        }
        passes.remove(id);
    }

    private IndividualTicket purchasableTicket(String ticketIdentifier) {
        String id = requireText(ticketIdentifier, "ticket identifier");
        IndividualTicket ticket = findIndividualTicket(id);
        if (ticket == null) {
            throw new ElementNotFoundException("individual ticket not found: " + id);
        }
        if (ticket.getDate().isBefore(currentDate)) {
            throw new OperationNotAllowedException("ticket " + id + " is for a past date");
        }
        return ticket;
    }

    private void removePassesOf(IndividualTicket ticket) {
        passes.values().removeIf(pass -> pass.getTicket() == ticket);
    }

    // --- attractions -----------------------------------------------------

    public Attraction<?> findAttraction(String identifier) {
        return attractions.get(identifier);
    }

    public Collection<Attraction<?>> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    /** Creates a GENERAL attraction (Delivery 2 syntax). */
    public Attraction<?> createAttraction(String identifier, String name, String capacityText) {
        return createAttraction(identifier, name, capacityText, AttractionType.GENERAL.name());
    }

    public Attraction<?> createAttraction(String identifier, String name, String capacityText, String typeText) {
        String id = requireText(identifier, "attraction identifier");
        String attractionName = requireText(name, "attraction name");
        int capacity = parseCapacity(capacityText);
        AttractionType type = AttractionType.parse(typeText);
        if (attractions.containsKey(id)) {
            throw new DuplicateElementException("attraction already exists: " + id);
        }
        Attraction<?> attraction = type.create(id, attractionName, capacity);
        attractions.put(id, attraction);
        return attraction;
    }

    public Attraction<?> existingAttraction(String identifier) {
        String id = requireText(identifier, "attraction identifier");
        Attraction<?> attraction = attractions.get(id);
        if (attraction == null) {
            throw new ElementNotFoundException("attraction not found: " + id);
        }
        return attraction;
    }

    /**
     * Lets the users admitted by a ticket enter an attraction. The ticket must
     * be for the current date and the attraction must have room for all of
     * them; otherwise nobody enters.
     *
     * @return the users who entered
     */
    /** Normal entry (Delivery 2 syntax). */
    public Entry enterAttraction(String attractionIdentifier, String ticketIdentifier) {
        return enterAttraction(attractionIdentifier, ticketIdentifier, false);
    }

    /** Entry optionally using SpeedPasses; Special Passes are consumed automatically. */
    public Entry enterAttraction(String attractionIdentifier, String ticketIdentifier, boolean speed) {
        Attraction<?> attraction = existingAttraction(attractionIdentifier);
        Ticket ticket = existingTopLevelTicket(ticketIdentifier);
        return enter(attraction, ticket, speed);
    }

    /** Entry through the attraction's own access capability {@code T}. */
    private <T extends AccessTicket> Entry enter(Attraction<T> attraction, Ticket ticket, boolean speed) {
        T access = attraction.accessFor(ticket);
        return attraction.enter(access, currentDate, passLookup, speed);
    }

    private final PassLookup passLookup = new PassLookup() {
        @Override
        public Optional<Pass> unconsumedSpeedPass(IndividualTicket ticket) {
            return firstUnconsumed(SpeedPass.class, ticket);
        }

        @Override
        public Optional<Pass> unconsumedSpecialPass(IndividualTicket ticket) {
            return firstUnconsumed(SpecialPass.class, ticket);
        }
    };

    private Optional<Pass> firstUnconsumed(Class<? extends Pass> type, IndividualTicket ticket) {
        for (Pass pass : passes.values()) {
            if (type.isInstance(pass) && pass.getTicket() == ticket && !pass.isConsumed()) {
                return Optional.of(pass);
            }
        }
        return Optional.empty();
    }

    /** Removes the user with that email from the attraction, releasing the place. */
    public User exitAttraction(String attractionIdentifier, String email) {
        Attraction<?> attraction = existingAttraction(attractionIdentifier);
        String address = requireText(email, "email");
        for (User user : attraction.getOccupants()) {
            if (user.getEmail().equals(address)) {
                attraction.removeOccupant(user);
                return user;
            }
        }
        throw new OperationNotAllowedException("user " + address + " is not inside attraction "
                + attraction.getIdentifier());
    }

    private Ticket existingTopLevelTicket(String identifier) {
        String id = requireText(identifier, "ticket identifier");
        Ticket ticket = tickets.get(id);
        if (ticket != null) {
            return ticket;
        }
        for (Ticket candidate : tickets.values()) {
            if (candidate.identifiers().contains(id)) {
                throw new OperationNotAllowedException("ticket " + id + " belongs to group ticket "
                        + candidate.getIdentifier() + "; use the group ticket");
            }
        }
        throw new ElementNotFoundException("ticket not found: " + id);
    }

    private static int parseCapacity(String text) {
        String value = text == null ? "" : text.trim();
        boolean digitsOnly = !value.isEmpty();
        for (int i = 0; i < value.length() && digitsOnly; i++) {
            digitsOnly = value.charAt(i) >= '0' && value.charAt(i) <= '9';
        }
        int capacity = 0;
        if (digitsOnly) {
            try {
                capacity = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                capacity = 0;
            }
        }
        if (capacity <= 0) {
            throw new InvalidValueException("capacity must be a whole number greater than zero: " + text);
        }
        return capacity;
    }

    // --- tickets ---------------------------------------------------------

    public Ticket findTicket(String identifier) {
        return tickets.get(identifier);
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    /** Common check of the identifier space shared by every ticket type. */
    public boolean isTicketIdentifierUsed(String identifier) {
        for (Ticket ticket : tickets.values()) {
            if (ticket.identifiers().contains(identifier)) {
                return true;
            }
        }
        return false;
    }

    public IndividualTicket createIndividualTicket(String identifier, String email, String dateText) {
        String id = requireText(identifier, "ticket identifier");
        User user = existingUser(email);
        LocalDate date = parseDate(dateText);
        requireNotPast(date);
        IndividualTicket ticket = new IndividualTicket(id, user, date, basePrice);
        addTicket(ticket);
        return ticket;
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, String dateText) {
        String id = requireText(identifier, "ticket identifier");
        Group group = existingGroup(groupIdentifier);
        LocalDate date = parseDate(dateText);
        requireNotPast(date);
        Set<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new OperationNotAllowedException("group " + group.getIdentifier() + " has no members");
        }
        BigDecimal price = GroupTicket.discountedPrice(basePrice, members.size());
        List<IndividualTicket> composition = new ArrayList<>();
        for (User member : members) {
            composition.add(new IndividualTicket(
                    GroupTicket.componentIdentifier(id, member.getDni()), member, date, price));
        }
        GroupTicket ticket = new GroupTicket(id, group, date, composition);
        addTicket(ticket);
        return ticket;
    }

    public void deleteGroupTicket(String identifier) {
        String id = requireText(identifier, "ticket identifier");
        if (!(tickets.get(id) instanceof GroupTicket ticket)) {
            throw new ElementNotFoundException("group ticket not found: " + id);
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new OperationNotAllowedException("group ticket " + id
                    + " is for the current date or earlier and must be kept");
        }
        for (IndividualTicket component : ticket.getComposition()) {
            removePassesOf(component);
        }
        tickets.remove(id);
    }

    /**
     * A user may be deleted only without active tickets (including those inside
     * group tickets) and when every group they belong to can be modified.
     */
    public boolean canDeleteUser(User user) {
        if (hasActiveTicket(user)) {
            return false;
        }
        for (Group group : groups.values()) {
            if (group.hasMember(user) && hasActiveGroupTicket(group)) {
                return false;
            }
        }
        return true;
    }

    public void deleteTicket(String identifier) {
        String id = requireText(identifier, "ticket identifier");
        Ticket ticket = tickets.get(id);
        if (ticket instanceof GroupTicket) {
            throw new OperationNotAllowedException("ticket " + id
                    + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (ticket == null) {
            for (Ticket candidate : tickets.values()) {
                if (candidate.identifiers().contains(id)) {
                    throw new OperationNotAllowedException("ticket " + id + " belongs to group ticket "
                            + candidate.getIdentifier() + " and cannot be deleted directly");
                }
            }
            throw new ElementNotFoundException("ticket not found: " + id);
        }
        if (!ticket.isDeletableOn(currentDate)) {
            throw new OperationNotAllowedException("ticket " + id
                    + " is for the current date or earlier and must be kept");
        }
        removePassesOf((IndividualTicket) ticket);
        tickets.remove(id);
    }

    /** Tickets of a user, obtained by traversing the tickets managed by the park. */
    public List<Ticket> ticketsOf(User user) {
        return tickets.values().stream().filter(t -> t.involves(user)).toList();
    }

    /** Derived state: whether the user has a ticket for the current date or later. */
    public boolean hasActiveTicket(User user) {
        for (Ticket ticket : ticketsOf(user)) {
            if (ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    private void addTicket(Ticket ticket) {
        Set<String> seen = new HashSet<>();
        for (String id : ticket.identifiers()) {
            if (isTicketIdentifierUsed(id) || !seen.add(id)) {
                throw new DuplicateElementException("ticket identifier already in use: " + id);
            }
        }
        tickets.put(ticket.getIdentifier(), ticket);
    }

    private void requireNotPast(LocalDate date) {
        if (date.isBefore(currentDate)) {
            throw new InvalidValueException("date " + date
                    + " is earlier than the current date " + currentDate);
        }
    }

    /**
     * A group has an active group ticket when one of its group tickets is
     * dated on or after the current system date. The state is always computed
     * from the tickets, never stored.
     */
    public boolean hasActiveGroupTicket(Group group) {
        for (GroupTicket ticket : groupTicketsOf(group)) {
            if (ticket.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    /** Group tickets purchased by the group, found by traversing the park tickets. */
    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket ticket : tickets.values()) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                result.add(groupTicket);
            }
        }
        return result;
    }

    private void requireModifiable(Group group) {
        if (hasActiveGroupTicket(group)) {
            throw new OperationNotAllowedException("group " + group.getIdentifier()
                    + " cannot be modified while it has an active group ticket");
        }
    }

    public Group existingGroup(String identifier) {
        String id = requireText(identifier, "group identifier");
        Group group = groups.get(id);
        if (group == null) {
            throw new ElementNotFoundException("group not found: " + id);
        }
        return group;
    }

    public User existingUser(String email) {
        String address = requireText(email, "email");
        User user = users.get(address);
        if (user == null) {
            throw new ElementNotFoundException("user not found: " + address);
        }
        return user;
    }

    private static String requireText(String value, String what) {
        String text = value == null ? "" : value.trim();
        if (text.isEmpty()) {
            throw new InvalidValueException(what + " must not be empty");
        }
        return text;
    }
}
