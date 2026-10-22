package es.upm.amusementpark.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * A park attraction with a maximum capacity. It knows the users currently
 * inside it; users do not know which attraction they are in.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();

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

    public List<User> getOccupants() {
        return Collections.unmodifiableList(occupants);
    }

    public int getFreePlaces() {
        return capacity - occupants.size();
    }

    public boolean hasRoomFor(int people) {
        return people <= getFreePlaces();
    }

    public boolean contains(User user) {
        return occupants.contains(user);
    }

    /**
     * Lets all {@code users} in together.
     *
     * @throws IllegalStateException if they do not all fit
     */
    public void enter(Collection<User> users) {
        if (!hasRoomFor(users.size())) {
            throw new IllegalStateException("Not enough room in attraction " + id);
        }
        occupants.addAll(users);
    }

    /** Returns the occupant with that email (ignoring case), or {@code null}. */
    public User findOccupant(String email) {
        String wanted = email.toLowerCase(Locale.ROOT);
        for (User user : occupants) {
            if (user.getEmail().toLowerCase(Locale.ROOT).equals(wanted)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Removes {@code user} from the attraction.
     *
     * @return the position the user had, or {@code -1} if not inside
     */
    public int exit(User user) {
        int position = occupants.indexOf(user);
        if (position >= 0) {
            occupants.remove(position);
        }
        return position;
    }

    /** Puts {@code user} back at {@code position}, used to undo an exit. */
    public void restore(int position, User user) {
        if (!contains(user)) {
            occupants.add(Math.min(position, occupants.size()), user);
        }
    }
}
