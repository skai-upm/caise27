package es.upm.park.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. Entry and exit
 * work for any {@link Ticket}: the ticket supplies its holders.
 */
public class Attraction {

    private static final int MAX_CAPACITY_DIGITS = 9;

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> present = new ArrayList<>();

    public Attraction(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    /** Positive whole number written with ASCII digits, or {@code null}. */
    public static Integer parseCapacity(String text) {
        if (text.isEmpty() || text.length() > MAX_CAPACITY_DIGITS) {
            return null;
        }
        for (char c : text.toCharArray()) {
            if (c < '0' || c > '9') {
                return null;
            }
        }
        int value = Integer.parseInt(text);
        return value > 0 ? value : null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<User> getPresent() {
        return Collections.unmodifiableList(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    public boolean isPresent(User user) {
        return present.stream().anyMatch(u -> u == user);
    }

    /** Admits all holders of the ticket or none of them. */
    public Result enter(Ticket ticket, LocalDate today) {
        if (!ticket.getDate().equals(today)) {
            return Result.error("Ticket " + ticket.getId() + " is valid for " + ticket.getDate()
                    + ", not for the system date " + today + ".");
        }
        List<User> holders = ticket.getHolders();
        for (User u : holders) {
            if (isPresent(u)) {
                return Result.error("User " + u.getEmail() + " is already in attraction " + id + ".");
            }
        }
        if (holders.size() > getAvailable()) {
            return Result.error("Attraction " + id + " has " + getAvailable() + " available places; "
                    + holders.size() + " needed.");
        }
        present.addAll(holders);
        return Result.ok(holders.size() + " user(s) entered attraction " + id + " with ticket " + ticket.getId() + ".");
    }

    /** Releases the holders of the ticket that are inside; the ticket date is not checked. */
    public Result exit(Ticket ticket) {
        int before = present.size();
        for (User u : ticket.getHolders()) {
            present.removeIf(p -> p == u);
        }
        int left = before - present.size();
        if (left == 0) {
            return Result.error("No user of ticket " + ticket.getId() + " is in attraction " + id + ".");
        }
        return Result.ok(left + " user(s) left attraction " + id + ".");
    }

    /** Removes a user if present (e.g. after the user was deleted). */
    public void remove(User user) {
        present.removeIf(p -> p == user);
    }
}
