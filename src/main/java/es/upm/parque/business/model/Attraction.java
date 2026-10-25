package es.upm.parque.business.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A park attraction with a maximum capacity and the users currently inside.
 * The type parameter bounds the kind of access (ticket) that the attraction admits.
 *
 * @param <T> admitted access type
 */
public abstract class Attraction<T extends Access> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> accessType;
    private final List<User> users = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> accessType) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.accessType = accessType;
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

    public List<User> getUsers() {
        return List.copyOf(users);
    }

    public int getAvailable() {
        return capacity - users.size();
    }

    public abstract AttractionType getType();

    /**
     * True if this attraction admits the given kind of access.
     */
    public boolean admits(Access access) {
        return accessType.isInstance(access);
    }

    /**
     * Lets in every user of the access, or nobody. With speed, one unused SpeedPass of every individual
     * ticket involved is consumed. Nothing changes when the entry is rejected.
     */
    public EntryResult enter(Access access, LocalDate currentDate, boolean speed) {
        if (!admits(access)) {
            return EntryResult.rejected(getType() + " attraction " + id + " does not admit ticket " + access.getId());
        }
        T admitted = accessType.cast(access);
        if (!admitted.getDate().equals(currentDate)) {
            return EntryResult.rejected("ticket " + access.getId() + " is not valid for " + currentDate);
        }
        List<User> entering = admitted.getUsers();
        for (User u : entering) {
            if (users.contains(u)) {
                return EntryResult.rejected("user " + u.getEmail() + " is already in the attraction");
            }
        }
        if (entering.size() > getAvailable()) {
            return EntryResult.rejected("not enough capacity (" + getAvailable() + " available, "
                    + entering.size() + " required)");
        }
        String error = speed ? checkSpeed(admitted) : checkSpecific(admitted);
        if (error != null) {
            return EntryResult.rejected(error);
        }
        users.addAll(entering);
        List<Pass> consumed = speed ? consumeSpeed(admitted) : consumeSpecific(admitted);
        return EntryResult.admitted(entering.size(), consumed);
    }

    /**
     * Checks that SPEED entry is possible: every individual ticket has an unused SpeedPass.
     */
    protected String checkSpeed(T access) {
        for (IndividualTicket t : access.getIndividualTickets()) {
            if (t.firstUnusedSpeedPass() == null) {
                return "ticket " + t.getId() + " has no unused SpeedPass";
            }
        }
        return null;
    }

    private List<Pass> consumeSpeed(T access) {
        List<Pass> consumed = new ArrayList<>();
        for (IndividualTicket t : access.getIndividualTickets()) {
            SpeedPass pass = t.firstUnusedSpeedPass();
            pass.consume();
            consumed.add(pass);
        }
        return consumed;
    }

    /**
     * Additional admission rules of a normal (non-SPEED) entry. None by default.
     */
    protected String checkSpecific(T access) {
        return null;
    }

    /**
     * Passes consumed by a successful normal entry. None by default.
     */
    protected List<Pass> consumeSpecific(T access) {
        return List.of();
    }

    /**
     * Removes the users of the access that are inside. Returns how many left.
     */
    public int exit(Access access) {
        int before = users.size();
        users.removeAll(access.getUsers());
        return before - users.size();
    }
}
