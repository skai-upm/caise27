package es.upm.amusementpark.business.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.amusementpark.business.OperationException;

/**
 * A park attraction with a maximum capacity that admits one kind of ticket.
 * The type parameter is the access capability this attraction accepts: only
 * admissions of that type can {@link #enter} it. The users present are
 * runtime state only and start empty every execution.
 *
 * @param <T> access capability admitted by the attraction
 */
public abstract class Attraction<T extends Admission> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final List<User> visitors = new ArrayList<>();

    protected Attraction(String id, String name, int capacity, Class<T> admitted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admitted = admitted;
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

    public abstract AttractionType getType();

    public List<User> getVisitors() {
        return Collections.unmodifiableList(visitors);
    }

    public int getAvailablePlaces() {
        return capacity - visitors.size();
    }

    /** Whether this attraction accepts the kind of the given admission. */
    public boolean admits(Admission admission) {
        return admitted.isInstance(admission);
    }

    /** Checks the admission kind and lets it {@link #enter}; returns the passes consumed. */
    public List<Pass> admit(Admission admission, boolean speed) {
        if (!admits(admission)) {
            throw new OperationException(getType() + " attraction " + id + " does not admit "
                    + (admission instanceof GroupAdmission ? "group" : "individual") + " tickets");
        }
        return enter(admitted.cast(admission), speed);
    }

    /**
     * Admits every user of the admission, or nobody: capacity, presence and the
     * required passes are all checked before anything changes. On success the
     * required passes are consumed and returned.
     */
    public List<Pass> enter(T admission, boolean speed) {
        List<User> entering = admission.getUsers();
        for (User user : entering) {
            if (visitors.contains(user)) {
                throw new OperationException(user.getEmail() + " is already in attraction " + id);
            }
        }
        if (entering.size() > getAvailablePlaces()) {
            throw new OperationException("attraction " + id + " has " + getAvailablePlaces()
                    + " available places but the ticket admits " + entering.size());
        }
        List<Pass> passes = passesRequired(admission, speed);
        visitors.addAll(entering);
        passes.forEach(Pass::consume);
        return passes;
    }

    /**
     * Passes that must be consumed for this entry. With {@code speed}, every
     * individual admission needs an unused SpeedPass; otherwise none is needed.
     */
    protected List<Pass> passesRequired(T admission, boolean speed) {
        List<Pass> passes = new ArrayList<>();
        if (speed) {
            for (IndividualAdmission individual : admission.getIndividualAdmissions()) {
                SpeedPass pass = individual.findUnusedSpeedPass();
                if (pass == null) {
                    throw new OperationException("ticket " + individual.getId() + " has no unused SpeedPass");
                }
                passes.add(pass);
            }
        }
        return passes;
    }

    /** Undoes an entry whose result could not be saved. */
    public void cancelEntry(Admission admission, List<Pass> consumed) {
        visitors.removeAll(admission.getUsers());
        consumed.forEach(Pass::restore);
    }

    /** Removes the admission's users who are present and returns how many left. */
    public int exit(Admission admission) {
        List<User> leaving = new ArrayList<>();
        for (User user : admission.getUsers()) {
            if (visitors.contains(user)) {
                leaving.add(user);
            }
        }
        if (leaving.isEmpty()) {
            throw new OperationException("no user of ticket " + admission.getId() + " is in attraction " + id);
        }
        visitors.removeAll(leaving);
        return leaving.size();
    }

    public void removeVisitor(User user) {
        visitors.remove(user);
    }
}
