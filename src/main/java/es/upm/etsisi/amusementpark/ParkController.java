package es.upm.etsisi.amusementpark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central park management. A single instance exists per execution; it owns the
 * users, groups, tickets and attractions, the current system date and the
 * current base ticket price.
 *
 * <p>Every operation validates first, then applies its change and saves all
 * repositories. If saving fails, the change is undone so that a rejected or
 * failed operation never leaves partial state.
 */
public final class ParkController {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final ParkController INSTANCE = new ParkController();

    private ParkRepositories repositories;
    private List<User> users = new ArrayList<>();
    private List<Group> groups = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();
    private List<Attraction> attractions = new ArrayList<>();
    private LocalDate today = LocalDate.now();
    private BigDecimal basePrice = INITIAL_PRICE;

    private ParkController() {
    }

    public static ParkController getInstance() {
        return INSTANCE;
    }

    /** Starts a new execution: loads persisted data and resets the date and price. */
    public void initialize(ParkRepositories parkRepositories, LocalDate startDate) throws ParkException {
        ParkRepositories.ParkData data = parkRepositories.load();
        this.repositories = parkRepositories;
        this.users = new ArrayList<>(data.users());
        this.groups = new ArrayList<>(data.groups());
        this.tickets = new ArrayList<>(data.tickets());
        this.attractions = new ArrayList<>(data.attractions());
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

    public void setDate(String value) throws ParkException {
        LocalDate date = Values.parseDate(value);
        if (date == null) {
            throw new ParkException("invalid date, expected yyyy-MM-dd");
        }
        today = date;
    }

    public void setPrice(String value) throws ParkException {
        BigDecimal price = Values.parsePrice(value);
        if (price == null) {
            throw new ParkException("invalid price, expected a positive amount with at most two decimals");
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
            throw new ParkException("invalid DNI");
        }
        validateEmail(email);
        validatePassword(password);
        validateCard(card);
        for (User user : users) {
            if (user.getDni().equals(dni)) {
                throw new ParkException("a user with DNI " + dni + " already exists");
            }
        }
        if (findUser(email) != null) {
            throw new ParkException("a user with email " + email + " already exists");
        }
        User user = new User(firstName, surnames, dni, email, password, phone, card);
        users.add(user);
        commit(() -> users.remove(user));
    }

    public void deleteUser(String email, String password) throws ParkException {
        User user = authenticate(email, password);
        if (hasActiveTicket(user)) {
            throw new ParkException("the user has active tickets");
        }
        Map<Group, Integer> memberships = new LinkedHashMap<>();
        for (Group group : groups) {
            if (group.hasMember(user)) {
                if (isLocked(group)) {
                    throw new ParkException("the user belongs to group " + group.getIdentifier()
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
        for (Attraction attraction : attractions) {
            attraction.release(user);
        }
    }

    public void updateUser(String email, String password, String fieldName, String newValue) throws ParkException {
        User user = authenticate(email, password);
        UserField field = UserField.fromName(fieldName);
        if (field == null) {
            throw new ParkException("field " + fieldName + " cannot be modified");
        }
        switch (field) {
            case EMAIL -> {
                validateEmail(newValue);
                User owner = findUser(newValue);
                if (owner != null && owner != user) {
                    throw new ParkException("a user with email " + newValue + " already exists");
                }
            }
            case PASSWORD -> validatePassword(newValue);
            case CARD -> validateCard(newValue);
            default -> {
                // NAME, SURNAME and PHONE have no validation rules
            }
        }
        String oldValue = readField(user, field);
        writeField(user, field, newValue);
        commit(() -> writeField(user, field, oldValue));
    }

    private User authenticate(String email, String password) throws ParkException {
        User user = findUser(email);
        if (user == null) {
            throw new ParkException("no user with email " + email);
        }
        if (!user.checkPassword(password)) {
            throw new ParkException("incorrect password");
        }
        return user;
    }

    private User requireUser(String email) throws ParkException {
        User user = findUser(email);
        if (user == null) {
            throw new ParkException("no user with email " + email);
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
            case PASSWORD -> user.getPassword();
            case PHONE -> user.getPhone();
            case CARD -> user.getCard();
        };
    }

    private static void writeField(User user, UserField field, String value) {
        switch (field) {
            case NAME -> user.setFirstName(value);
            case SURNAME -> user.setSurnames(value);
            case EMAIL -> user.setEmail(value);
            case PASSWORD -> user.setPassword(value);
            case PHONE -> user.setPhone(value);
            case CARD -> user.setCard(value);
        }
    }

    private static void validateEmail(String email) throws ParkException {
        if (!UserValidator.isValidEmail(email)) {
            throw new ParkException("invalid email");
        }
    }

    private static void validatePassword(String password) throws ParkException {
        if (!UserValidator.isValidPassword(password)) {
            throw new ParkException("invalid password");
        }
    }

    private static void validateCard(String card) throws ParkException {
        if (!UserValidator.isValidCard(card)) {
            throw new ParkException("invalid card number");
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
            throw new ParkException("a group with identifier " + identifier + " already exists");
        }
        Group group = new Group(identifier, name);
        groups.add(group);
        commit(() -> groups.remove(group));
    }

    public void addToGroup(String identifier, String email) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        User user = requireUser(email);
        if (group.hasMember(user)) {
            throw new ParkException("user " + email + " already belongs to group " + identifier);
        }
        group.addMember(user);
        commit(() -> group.removeMember(user));
    }

    public void removeFromGroup(String identifier, String email) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        User user = requireUser(email);
        if (!group.hasMember(user)) {
            throw new ParkException("user " + email + " does not belong to group " + identifier);
        }
        int position = group.indexOf(user);
        group.removeMember(user);
        commit(() -> group.addMember(position, user));
    }

    public void deleteGroup(String identifier) throws ParkException {
        Group group = requireModifiableGroup(identifier);
        List<GroupTicket> historical = new ArrayList<>();
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group) {
                historical.add(groupTicket);
            }
        }
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
        for (Ticket ticket : tickets) {
            if (ticket instanceof GroupTicket groupTicket && groupTicket.getGroup() == group
                    && groupTicket.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    private Group requireGroup(String identifier) throws ParkException {
        Group group = findGroup(identifier);
        if (group == null) {
            throw new ParkException("no group with identifier " + identifier);
        }
        return group;
    }

    private Group requireModifiableGroup(String identifier) throws ParkException {
        Group group = requireGroup(identifier);
        if (isLocked(group)) {
            throw new ParkException("group " + identifier + " has an active group ticket and cannot be modified");
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
            throw new ParkException("no ticket with identifier " + identifier);
        }
        if (ticket instanceof GroupTicket) {
            throw new ParkException("ticket " + identifier + " is a group ticket; use GROUPTICKET DELETE");
        }
        if (!tickets.contains(ticket)) {
            throw new ParkException("ticket " + identifier + " belongs to a group ticket and cannot be deleted alone");
        }
        removeTicket(ticket);
    }

    public GroupTicket createGroupTicket(String identifier, String groupIdentifier, String date) throws ParkException {
        requireText(identifier, "ticket identifier");
        Group group = requireGroup(groupIdentifier);
        LocalDate day = requireUsableDate(date);
        List<User> members = group.getMembers();
        if (members.isEmpty()) {
            throw new ParkException("group " + groupIdentifier + " has no members");
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
            throw new ParkException("no group ticket with identifier " + identifier);
        }
        removeTicket(ticket);
    }

    private void removeTicket(Ticket ticket) throws ParkException {
        if (!ticket.isDeletableOn(today)) {
            throw new ParkException("only tickets dated after " + today + " can be deleted");
        }
        int index = tickets.indexOf(ticket);
        tickets.remove(ticket);
        commit(() -> tickets.add(index, ticket));
    }

    private LocalDate requireUsableDate(String value) throws ParkException {
        LocalDate day = Values.parseDate(value);
        if (day == null) {
            throw new ParkException("invalid date, expected yyyy-MM-dd");
        }
        if (day.isBefore(today)) {
            throw new ParkException("tickets cannot be created for past dates");
        }
        return day;
    }

    private void requireFreeIdentifier(String identifier) throws ParkException {
        if (findTicket(identifier) != null) {
            throw new ParkException("a ticket with identifier " + identifier + " already exists");
        }
    }

    // ----------------------------------------------------------- attractions

    public List<Attraction> getAttractions() {
        return List.copyOf(attractions);
    }

    public void createAttraction(String identifier, String name, String capacity) throws ParkException {
        requireText(identifier, "attraction identifier");
        requireText(name, "attraction name");
        int places = Values.parseCapacity(capacity);
        if (places <= 0) {
            throw new ParkException("capacity must be a positive integer");
        }
        if (findAttraction(identifier) != null) {
            throw new ParkException("an attraction with identifier " + identifier + " already exists");
        }
        Attraction attraction = new Attraction(identifier, name, places);
        attractions.add(attraction);
        commit(() -> attractions.remove(attraction));
    }

    public Attraction requireAttraction(String identifier) throws ParkException {
        Attraction attraction = findAttraction(identifier);
        if (attraction == null) {
            throw new ParkException("no attraction with identifier " + identifier);
        }
        return attraction;
    }

    /**
     * Admits every holder of the ticket, or nobody: the ticket must be valid today
     * and the attraction must have room for all of its holders.
     *
     * @return the number of users admitted
     */
    public int enter(String attractionIdentifier, String ticketIdentifier) throws ParkException {
        Attraction attraction = requireAttraction(attractionIdentifier);
        Ticket ticket = findTicket(ticketIdentifier);
        if (ticket == null) {
            throw new ParkException("no ticket with identifier " + ticketIdentifier);
        }
        if (!ticket.isValidOn(today)) {
            throw new ParkException("ticket " + ticketIdentifier + " is not valid on " + today);
        }
        List<User> holders = ticket.getHolders();
        for (User holder : holders) {
            if (!users.contains(holder)) {
                throw new ParkException("user " + holder.getEmail() + " is no longer registered");
            }
            if (attraction.isInside(holder)) {
                throw new ParkException("user " + holder.getEmail() + " is already inside " + attractionIdentifier);
            }
        }
        if (attraction.getAvailablePlaces() < holders.size()) {
            throw new ParkException("not enough places in " + attractionIdentifier + " for " + holders.size()
                    + " user(s)");
        }
        attraction.admit(holders);
        return holders.size();
    }

    public void exit(String attractionIdentifier, String email) throws ParkException {
        Attraction attraction = requireAttraction(attractionIdentifier);
        for (User occupant : attraction.getOccupants()) {
            if (occupant.getEmail().equals(email)) {
                attraction.release(occupant);
                return;
            }
        }
        throw new ParkException("user " + email + " is not inside " + attractionIdentifier);
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

    private static void requireText(String value, String description) throws ParkException {
        if (value.isBlank()) {
            throw new ParkException(description + " must not be empty");
        }
    }

    private void commit(Runnable undo) throws ParkException {
        try {
            repositories.save(snapshot());
        } catch (ParkException e) {
            undo.run();
            restoreFiles();
            throw e;
        }
    }

    private void restoreFiles() {
        try {
            repositories.save(snapshot());
        } catch (ParkException e) {
            // The original failure is reported to the caller; memory is already consistent.
        }
    }

    private ParkRepositories.ParkData snapshot() {
        return new ParkRepositories.ParkData(users, groups, tickets, attractions);
    }
}
