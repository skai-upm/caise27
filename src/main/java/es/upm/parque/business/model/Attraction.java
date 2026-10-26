package es.upm.parque.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside it. The type parameter
 * bounds the kind of ticket (access capability) the attraction admits.
 *
 * @param <T> access capability accepted by this attraction
 */
public abstract class Attraction<T extends Admission> {

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

    public abstract AttractionType getType();

    /**
     * True when the ticket offers the access capability this attraction admits.
     */
    public boolean accepts(Admission admission) {
        return admitted.isInstance(admission);
    }

    /**
     * Converts an accepted ticket to the admitted capability.
     */
    public T cast(Admission admission) {
        return admitted.cast(admission);
    }

    public boolean hasRoomFor(T admission) {
        return canAdmit(admission.getVisitors());
    }

    /**
     * Lets in every user admitted by the ticket.
     */
    public void enter(T admission) {
        admit(admission.getVisitors());
    }

    /**
     * Parses a positive integer capacity, or returns null when the text is not valid.
     */
    public static Integer parseCapacity(String text) {
        try {
            int value = Integer.parseInt(text);
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
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

    public int available() {
        return capacity - present.size();
    }

    public boolean isEmpty() {
        return present.isEmpty();
    }

    public boolean contains(User user) {
        return present.contains(user);
    }

    public boolean canAdmit(List<User> users) {
        return users.size() <= available();
    }

    public void admit(List<User> users) {
        present.addAll(users);
    }

    /**
     * Removes the given users that are present and returns how many left.
     */
    public int release(List<User> users) {
        int before = present.size();
        present.removeAll(users);
        return before - present.size();
    }
}
