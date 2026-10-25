package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * The type parameter bounds which kind of admission the attraction accepts.
 *
 * @param <T> admissions accepted by this attraction
 */
public abstract class Attraction<T extends Admission> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accepted;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> accepted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accepted = accepted;
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
        return List.copyOf(present);
    }

    public int getAvailable() {
        return capacity - present.size();
    }

    public abstract AttractionType getType();

    /**
     * Lets in every user admitted, or nobody. Passes are consumed only when the entry succeeds.
     *
     * @param speed whether the visitor asked to use SpeedPasses
     */
    public EntryResult enter(Admission admission, boolean speed, LocalDate today) {
        if (!accepted.isInstance(admission)) {
            throw new IllegalArgumentException(getType() + " attraction " + id + " does not accept ticket "
                    + admission.getId());
        }
        T ticket = accepted.cast(admission);
        if (!ticket.isValidOn(today)) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " is not valid on " + today);
        }
        List<User> users = ticket.getUsers();
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Ticket " + ticket.getId() + " admits no users");
        }
        for (User u : users) {
            if (present.contains(u)) {
                throw new IllegalArgumentException("User " + u.getEmail() + " is already in " + id);
            }
        }
        if (users.size() > getAvailable()) {
            throw new IllegalArgumentException("Not enough capacity in " + id + ": " + getAvailable()
                    + " place(s) free, " + users.size() + " required");
        }
        List<Pass> passes = passesToConsume(ticket, speed);
        present.addAll(users);
        passes.forEach(Pass::consume);
        return new EntryResult(users.size(), passes);
    }

    /**
     * Passes that a successful entry consumes. By default, with SPEED every access ticket needs
     * one unused SpeedPass. Must not modify any state.
     */
    protected List<Pass> passesToConsume(T ticket, boolean speed) {
        List<Pass> passes = new ArrayList<>();
        if (speed) {
            for (IndividualTicket t : ticket.getAccessTickets()) {
                passes.add(t.firstUnusedSpeedPass().orElseThrow(() -> new IllegalArgumentException(
                        "Ticket " + t.getId() + " has no unused SpeedPass")));
            }
        }
        return passes;
    }

    /**
     * Lets out the admission's users that are inside.
     *
     * @return number of users that left
     */
    public int exit(Admission admission) {
        int before = present.size();
        present.removeAll(admission.getUsers());
        int left = before - present.size();
        if (left == 0) {
            throw new IllegalArgumentException("No user of ticket " + admission.getId() + " is in " + id);
        }
        return left;
    }
}
