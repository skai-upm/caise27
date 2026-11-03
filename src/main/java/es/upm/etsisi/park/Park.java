package es.upm.etsisi.park;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central park management. Exactly one instance exists per execution; it owns
 * users, groups, tickets and attractions together with the current system date
 * and base ticket price.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = new BigDecimal("40.00");

    private static final Park INSTANCE = new Park();

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Attraction> attractions = new LinkedHashMap<>();
    private ParkStorage storage;
    private LocalDate currentDate;
    private BigDecimal basePrice;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Prepares the park for an execution: today's date, the initial price and the
     * data loaded from the given storage.
     */
    public void start(ParkStorage storage, LocalDate today) throws IOException {
        users.clear();
        groups.clear();
        tickets.clear();
        attractions.clear();
        this.storage = storage;
        this.currentDate = today;
        this.basePrice = INITIAL_PRICE;

        for (User u : storage.users().load()) {
            users.put(u.getEmail(), u);
        }
        for (Attraction a : storage.attractions().load()) {
            attractions.put(a.getId(), a);
        }
        Map<String, User> byDni = new HashMap<>();
        for (User u : users.values()) {
            byDni.put(u.getDni(), u);
        }
        for (Group g : storage.groups().load(byDni)) {
            groups.put(g.getId(), g);
        }
        for (Ticket t : storage.tickets().load(byDni, groups)) {
            tickets.put(t.getId(), t);
        }
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    void setCurrentDate(LocalDate date) {
        this.currentDate = date;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    void setBasePrice(BigDecimal price) {
        this.basePrice = price;
    }

    // ---- users ----

    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    public User findUser(String email) {
        return users.get(email);
    }

    public User findUserByDni(String dni) {
        for (User u : users.values()) {
            if (u.getDni().equals(dni)) {
                return u;
            }
        }
        return null;
    }

    void addUser(User user) {
        users.put(user.getEmail(), user);
    }

    void removeUser(User user) {
        users.remove(user.getEmail());
    }

    /** Replaces the user collection, keeping the given order (used for rollbacks and email changes). */
    void replaceUsers(Map<String, User> snapshot) {
        users.clear();
        users.putAll(snapshot);
    }

    Map<String, User> snapshotUsers() {
        return new LinkedHashMap<>(users);
    }

    // ---- groups ----

    public List<Group> getGroups() {
        return new ArrayList<>(groups.values());
    }

    public Group findGroup(String id) {
        return groups.get(id);
    }

    void addGroup(Group group) {
        groups.put(group.getId(), group);
    }

    void replaceGroups(Map<String, Group> snapshot) {
        groups.clear();
        groups.putAll(snapshot);
    }

    Map<String, Group> snapshotGroups() {
        return new LinkedHashMap<>(groups);
    }

    public List<Group> groupsOf(User user) {
        List<Group> result = new ArrayList<>();
        for (Group g : groups.values()) {
            if (g.contains(user)) {
                result.add(g);
            }
        }
        return result;
    }

    /** A group is locked while any of its group tickets is dated today or later. */
    public boolean isGroupLocked(Group group) {
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket g && g.getGroup() == group && g.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    public List<GroupTicket> groupTicketsOf(Group group) {
        List<GroupTicket> result = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket g && g.getGroup() == group) {
                result.add(g);
            }
        }
        return result;
    }

    // ---- tickets ----

    /** Top-level tickets: standalone individual tickets and group tickets. */
    public List<Ticket> getTickets() {
        return new ArrayList<>(tickets.values());
    }

    /** Finds a ticket by identifier, including individual tickets inside group tickets. */
    public Ticket findTicket(String id) {
        Ticket ticket = tickets.get(id);
        if (ticket != null) {
            return ticket;
        }
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket g) {
                for (IndividualTicket c : g.getComposition()) {
                    if (c.getId().equals(id)) {
                        return c;
                    }
                }
            }
        }
        return null;
    }

    /** Whether the identifier is used by any ticket, of any type, in the shared identifier space. */
    public boolean isTicketIdInUse(String id) {
        return findTicket(id) != null;
    }

    /** Whether the ticket is a standalone (top-level) ticket rather than part of a group ticket. */
    public boolean isTopLevel(Ticket ticket) {
        return tickets.get(ticket.getId()) == ticket;
    }

    /** Every individual ticket, standalone or inside a group ticket. */
    public List<IndividualTicket> allIndividualTickets() {
        List<IndividualTicket> result = new ArrayList<>();
        for (Ticket t : tickets.values()) {
            if (t instanceof IndividualTicket i) {
                result.add(i);
            } else if (t instanceof GroupTicket g) {
                result.addAll(g.getComposition());
            }
        }
        return result;
    }

    /** A user is locked while any individual ticket of theirs is dated today or later. */
    public boolean hasActiveTicket(User user) {
        for (IndividualTicket t : allIndividualTickets()) {
            if (t.getUser() == user && t.isActiveOn(currentDate)) {
                return true;
            }
        }
        return false;
    }

    void addTicket(Ticket ticket) {
        tickets.put(ticket.getId(), ticket);
    }

    void removeTicket(Ticket ticket) {
        tickets.remove(ticket.getId());
    }

    void replaceTickets(Map<String, Ticket> snapshot) {
        tickets.clear();
        tickets.putAll(snapshot);
    }

    Map<String, Ticket> snapshotTickets() {
        return new LinkedHashMap<>(tickets);
    }

    // ---- attractions ----

    public List<Attraction> getAttractions() {
        return new ArrayList<>(attractions.values());
    }

    public Attraction findAttraction(String id) {
        return attractions.get(id);
    }

    void addAttraction(Attraction attraction) {
        attractions.put(attraction.getId(), attraction);
    }

    void removeAttraction(Attraction attraction) {
        attractions.remove(attraction.getId());
    }

    // ---- persistence ----

    /**
     * Saves the whole park. If saving fails, {@code undo} reverts the in-memory
     * change and the command is rejected.
     */
    void commit(Runnable undo) throws ValidationException {
        try {
            storage.saveAll(users.values(), groups.values(), tickets.values(), attractions.values());
        } catch (IOException e) {
            undo.run();
            throw new ValidationException("could not save park data: " + e.getMessage());
        }
    }
}
