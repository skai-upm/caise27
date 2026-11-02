package es.upm.etsisi.park.business;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central park management: the single holder of users, groups, tickets, passes and attractions,
 * together with the current system date and base ticket price. Controllers apply the rules;
 * every mutation is saved through the injected {@link ParkRepository} and undone if saving fails.
 */
public final class Park {

    public static final BigDecimal INITIAL_PRICE = Money.of(new BigDecimal("40"));

    private static final Park INSTANCE = new Park();

    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Group> groups = new LinkedHashMap<>();
    private final Map<String, Ticket> tickets = new LinkedHashMap<>();
    private final Map<String, Pass> passes = new LinkedHashMap<>();
    private final Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
    private ParkRepository repository;
    private LocalDate today;
    private BigDecimal basePrice;

    private Park() {
    }

    public static Park getInstance() {
        return INSTANCE;
    }

    /**
     * Starts an execution: loads persisted state, sets the system date to {@code startDate} and the
     * base price to 40.00. Attractions start empty.
     */
    public void start(ParkRepository parkRepository, LocalDate startDate) {
        ParkData data = parkRepository.load();
        users.clear();
        groups.clear();
        tickets.clear();
        passes.clear();
        attractions.clear();
        data.users().forEach(u -> users.put(u.getEmail(), u));
        data.groups().forEach(g -> groups.put(g.getId(), g));
        data.tickets().forEach(t -> tickets.put(t.getId(), t));
        data.passes().forEach(p -> passes.put(p.getId(), p));
        data.attractions().forEach(a -> attractions.put(a.getId(), a));
        this.repository = parkRepository;
        this.today = startDate;
        this.basePrice = INITIAL_PRICE;
    }

    // ---------------------------------------------------------------- system settings

    public LocalDate getToday() {
        return today;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setToday(String date) {
        this.today = ValueParser.parseDate(date);
    }

    public void setBasePrice(String price) {
        this.basePrice = ValueParser.parsePrice(price);
    }

    // ---------------------------------------------------------------- queries

    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    public User findUser(String email) {
        return users.get(email);
    }

    public Collection<Group> getGroups() {
        return Collections.unmodifiableCollection(groups.values());
    }

    public Group findGroup(String id) {
        return groups.get(id);
    }

    /** Top-level tickets: standalone individual tickets and group tickets. */
    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(tickets.values());
    }

    /** Finds any ticket in the shared identifier space, including group-ticket constituents. */
    public Ticket findTicket(String id) {
        Ticket top = tickets.get(id);
        if (top != null) {
            return top;
        }
        for (Ticket t : tickets.values()) {
            for (IndividualTicket c : t.getEntryTickets()) {
                if (c.getId().equals(id)) {
                    return c;
                }
            }
        }
        return null;
    }

    public Collection<Pass> getPasses() {
        return Collections.unmodifiableCollection(passes.values());
    }

    public Pass findPass(String id) {
        return passes.get(id);
    }

    /** Passes bought for the given individual ticket, in purchase order. */
    public List<Pass> passesOf(IndividualTicket ticket) {
        List<Pass> result = new ArrayList<>();
        for (Pass p : passes.values()) {
            if (p.getTicket() == ticket) {
                result.add(p);
            }
        }
        return result;
    }

    public Collection<Attraction<?>> getAttractions() {
        return Collections.unmodifiableCollection(attractions.values());
    }

    public Attraction<?> findAttraction(String id) {
        return attractions.get(id);
    }

    /** A group is locked while any of its group tickets is dated today or later. */
    public boolean isGroupLocked(Group group) {
        for (Ticket t : tickets.values()) {
            if (t instanceof GroupTicket g && g.getGroup() == group && g.isActiveOn(today)) {
                return true;
            }
        }
        return false;
    }

    /** A user is locked while any ticket that admits them is dated today or later. */
    public boolean hasActiveTicket(User user) {
        for (Ticket t : tickets.values()) {
            if (t.isActiveOn(today) && t.getHolders().contains(user)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------------- state access for controllers

    Map<String, User> users() {
        return users;
    }

    Map<String, Group> groups() {
        return groups;
    }

    Map<String, Ticket> tickets() {
        return tickets;
    }

    Map<String, Pass> passes() {
        return passes;
    }

    Map<String, Attraction<?>> attractions() {
        return attractions;
    }

    /** Saves the whole state; on failure runs {@code undo} and rethrows. */
    void commit(Runnable undo) {
        try {
            repository.save(new ParkData(List.copyOf(users.values()), List.copyOf(groups.values()),
                    List.copyOf(tickets.values()), List.copyOf(passes.values()), List.copyOf(attractions.values())));
        } catch (ParkException e) {
            undo.run();
            throw e;
        }
    }

    static <K, V> void restore(Map<K, V> target, Map<K, V> snapshot) {
        target.clear();
        target.putAll(snapshot);
    }

    static void requireNotBlank(String... values) {
        for (String v : values) {
            if (v == null || v.isBlank()) {
                throw new UserException("Malformed command: empty field");
            }
        }
    }
}
