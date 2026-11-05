package es.upm.park.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. The type parameter
 * bounds the kind of credential the attraction admits: entry is only performed through the
 * typed {@link #admit} method, after checking the credential's class.
 *
 * @param <T> class of credential this attraction admits
 */
public abstract class Attraction<T extends AccessCredential> {

    private static final int MAX_CAPACITY_DIGITS = 9;

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> admitted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admitted = admitted;
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

    public abstract AttractionType getType();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    /** Class of credential admitted by this attraction. */
    public Class<T> getAdmittedClass() {
        return admitted;
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

    /** Whether this attraction admits this kind of credential. */
    public boolean accepts(AccessCredential credential) {
        return admitted.isInstance(credential);
    }

    /** Admits all holders of the credential or none of them, if its kind is admitted. */
    public Result enter(AccessCredential credential, LocalDate today) {
        if (!accepts(credential)) {
            return Result.error("Attraction " + id + " (" + getType() + ") does not admit ticket "
                    + credential.getId() + ".");
        }
        return admit(admitted.cast(credential), today);
    }

    /** Typed admission: date, presence, type-specific conditions and capacity. */
    protected Result admit(T credential, LocalDate today) {
        if (!credential.getDate().equals(today)) {
            return Result.error("Ticket " + credential.getId() + " is valid for " + credential.getDate()
                    + ", not for the system date " + today + ".");
        }
        List<User> holders = credential.getHolders();
        for (User u : holders) {
            if (isPresent(u)) {
                return Result.error("User " + u.getEmail() + " is already in attraction " + id + ".");
            }
        }
        Result additional = checkAdditional(credential, today);
        if (additional != null) {
            return additional;
        }
        if (holders.size() > getAvailable()) {
            return Result.error("Attraction " + id + " has " + getAvailable() + " available places; "
                    + holders.size() + " needed.");
        }
        present.addAll(holders);
        return Result.ok(holders.size() + " user(s) entered attraction " + id + " with ticket " + credential.getId() + ".");
    }

    /** Type-specific admission condition: an error result, or {@code null} when satisfied. */
    protected Result checkAdditional(T credential, LocalDate today) {
        return null;
    }

    /** Releases the holders of the credential that are inside; the date is not checked. */
    public Result exit(AccessCredential credential) {
        int before = present.size();
        for (User u : credential.getHolders()) {
            present.removeIf(p -> p == u);
        }
        int left = before - present.size();
        if (left == 0) {
            return Result.error("No user of ticket " + credential.getId() + " is in attraction " + id + ".");
        }
        return Result.ok(left + " user(s) left attraction " + id + ".");
    }

    /** Removes a user if present (e.g. after the user was deleted). */
    public void remove(User user) {
        present.removeIf(p -> p == user);
    }
}
