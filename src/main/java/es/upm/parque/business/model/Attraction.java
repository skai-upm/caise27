package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. Users enter and leave through
 * any {@link Admission}; entry is all-or-nothing.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> present = new ArrayList<>();

    public Attraction(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
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

    /**
     * Lets in every user of the admission, or nobody if any rule fails.
     *
     * @throws IllegalArgumentException describing the violated rule
     */
    public void enter(Admission admission, LocalDate today) {
        if (!admission.isValidOn(today)) {
            throw new IllegalArgumentException("The ticket is not valid on " + today);
        }
        List<User> users = admission.getAdmittedUsers();
        if (users.isEmpty()) {
            throw new IllegalArgumentException("The ticket has no registered users");
        }
        for (User u : users) {
            if (present.contains(u)) {
                throw new IllegalArgumentException("User " + u.getEmail() + " is already in attraction " + id);
            }
        }
        if (users.size() > getAvailable()) {
            throw new IllegalArgumentException("Not enough capacity in attraction " + id + ": " + users.size()
                + " users, " + getAvailable() + " places available");
        }
        present.addAll(users);
    }

    /**
     * Lets out the users of the admission who are inside.
     *
     * @return the number of users who left
     * @throws IllegalArgumentException if none of them is inside
     */
    public int exit(Admission admission) {
        int before = present.size();
        present.removeAll(admission.getAdmittedUsers());
        int left = before - present.size();
        if (left == 0) {
            throw new IllegalArgumentException("No user of that ticket is in attraction " + id);
        }
        return left;
    }

    /** Removes a user regardless of ticket, used when the user is deleted. */
    public void remove(User user) {
        present.remove(user);
    }
}
