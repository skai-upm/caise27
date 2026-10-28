package es.upm.etsisi.amusementpark.business.model;

import es.upm.etsisi.amusementpark.business.exception.NotFoundException;
import es.upm.etsisi.amusementpark.business.exception.OperationNotAllowedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A park attraction. It references the users currently inside it; users do not
 * reference attractions. The type parameter is the ticket access capability the
 * attraction accepts: only admissions of type {@code T} can enter.
 *
 * @param <T> accepted access capability
 */
public abstract class Attraction<T extends Admission> {

    private final String identifier;
    private final String name;
    private final int capacity;
    private final List<User> occupants = new ArrayList<>();
    private final Class<T> admissionType;

    protected Attraction(String identifier, String name, int capacity, Class<T> admissionType) {
        this.identifier = identifier;
        this.name = name;
        this.capacity = capacity;
        this.admissionType = admissionType;
    }

    public abstract AttractionType getType();

    public boolean accepts(Admission admission) {
        return admissionType.isInstance(admission);
    }

    /**
     * Entry with a ticket of any type: rejected unless the attraction accepts it.
     *
     * @param passes passes associated with the admission's individual tickets
     * @param speed  whether SpeedPasses are used
     */
    public final EntryResult enterWith(Admission admission, Collection<Pass> passes, boolean speed) {
        if (!accepts(admission)) {
            throw new OperationNotAllowedException(getType() + " attraction " + identifier
                    + " does not accept ticket " + admission.getIdentifier());
        }
        return enter(admissionType.cast(admission), passes, speed);
    }

    /**
     * Lets the users of an accepted admission enter together and consumes the passes the
     * entry requires. Every condition is checked before anything changes: either all users
     * enter and the passes are consumed, or nothing happens.
     */
    public EntryResult enter(T admission, Collection<Pass> passes, boolean speed) {
        List<Pass> toConsume = passesToConsume(admission, passes, speed);
        List<User> users = admission.getIndividualTickets().stream().map(IndividualTicket::getUser).toList();
        admit(users);
        toConsume.forEach(Pass::consume);
        return new EntryResult(users, toConsume);
    }

    /**
     * Checks the pass conditions of the entry and selects, without modifying them, the
     * passes to consume. By default a SpeedPass entry needs one unconsumed SpeedPass per
     * individual ticket of the admission.
     */
    protected List<Pass> passesToConsume(T admission, Collection<Pass> passes, boolean speed) {
        if (!speed) {
            return List.of();
        }
        List<Pass> selected = new ArrayList<>();
        for (IndividualTicket part : admission.getIndividualTickets()) {
            selected.add(passes.stream()
                    .filter(pass -> pass instanceof SpeedPass && !pass.isConsumed() && pass.getTicket() == part)
                    .findFirst()
                    .orElseThrow(() -> new OperationNotAllowedException("Ticket " + part.getIdentifier()
                            + " has no unconsumed SpeedPass")));
        }
        return selected;
    }

    public String getIdentifier() {
        return identifier;
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

    public int getAvailablePlaces() {
        return capacity - occupants.size();
    }

    public Optional<User> findOccupant(String email) {
        return occupants.stream().filter(user -> user.getEmail().equals(email)).findFirst();
    }

    public boolean isFull() {
        return occupants.size() >= capacity;
    }

    public boolean isInside(User user) {
        return occupants.contains(user);
    }

    /**
     * @throws OperationNotAllowedException if the attraction is full or the user is already inside
     */
    public void addOccupant(User user) {
        if (isInside(user)) {
            throw new OperationNotAllowedException("User " + user.getEmail() + " is already inside attraction " + identifier);
        }
        if (isFull()) {
            throw new OperationNotAllowedException("Attraction " + identifier + " is full");
        }
        occupants.add(user);
    }

    /**
     * Admits all {@code users} together or none of them.
     *
     * @throws OperationNotAllowedException if any user is already inside or capacity is insufficient for all
     */
    public void admit(List<User> users) {
        for (User user : users) {
            if (isInside(user)) {
                throw new OperationNotAllowedException("User " + user.getEmail() + " is already inside attraction " + identifier);
            }
        }
        if (occupants.size() + users.size() > capacity) {
            throw new OperationNotAllowedException("Attraction " + identifier + " does not have capacity for "
                    + users.size() + " user(s)");
        }
        occupants.addAll(users);
    }

    /**
     * @throws NotFoundException if the user is not inside the attraction
     */
    public void removeOccupant(User user) {
        if (!occupants.remove(user)) {
            throw new NotFoundException("User " + user.getEmail() + " is not inside attraction " + identifier);
        }
    }
}
