package es.upm.amusementpark.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside. Which tickets it admits is decided by
 * its typed subclasses ({@link TypedAttraction}).
 */
public abstract class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> present = new ArrayList<>();

    protected Attraction(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public abstract AttractionType getType();

    /**
     * Admits all users of the ticket or none.
     *
     * @throws AccessDeniedException if any access condition fails; nothing changes in that case
     */
    public abstract void enter(AccessTicket ticket, LocalDate today) throws AccessDeniedException;

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

    /** Removes the given users that are inside and returns how many left. */
    public int exitAll(List<User> users) {
        int count = 0;
        for (User u : users) {
            if (present.remove(u)) {
                count++;
            }
        }
        return count;
    }

    /** Checks capacity and that no user is already inside, without changing anything. */
    protected void checkRoomFor(AccessTicket ticket) throws AccessDeniedException {
        List<User> users = ticket.getUsers();
        if (users.size() > getAvailable()) {
            throw new AccessDeniedException("not enough places in " + id + " (" + getAvailable() + " available, "
                    + users.size() + " requested)");
        }
        for (User u : users) {
            if (present.contains(u)) {
                throw new AccessDeniedException("some users of ticket " + ticket.getId() + " are already in " + id);
            }
        }
    }

    protected void admit(List<User> users) {
        present.addAll(users);
    }
}
