package es.upm.parque.business.model;

import es.upm.parque.business.BusinessException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A park attraction with a maximum capacity and the users currently inside it. The type parameter is the access
 * capability the attraction admits; entry is only possible with an object of that type.
 *
 * @param <T> the kind of access (ticket capability) admitted by the attraction
 */
public abstract class Attraction<T extends Accessible> {

    private final String id;
    private final String name;
    private final int capacity;
    private final Class<T> admitted;
    private final Set<User> visitors = new LinkedHashSet<>();

    protected Attraction(String id, String name, int capacity, Class<T> admitted) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.admitted = admitted;
    }

    public abstract AttractionType getType();

    /**
     * Whether entries may request the use of SpeedPasses.
     */
    public boolean acceptsSpeed() {
        return true;
    }

    /**
     * Whether every entry must use the ticket's Special Pass.
     */
    public boolean requiresSpecialPass() {
        return false;
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

    public List<User> getVisitors() {
        return List.copyOf(visitors);
    }

    public int available() {
        return capacity - visitors.size();
    }

    /**
     * Returns the access as the type this attraction admits, or rejects it.
     */
    public T admit(Accessible access) {
        if (!admitted.isInstance(access)) {
            throw new BusinessException("Attraction " + id + " (" + getType() + ") does not admit "
                    + kindOf(access) + " " + access.getId());
        }
        return admitted.cast(access);
    }

    /**
     * Admits an access of any kind if this attraction accepts it.
     *
     * @return the number of users admitted
     */
    public int enterWith(Accessible access, LocalDate today) {
        return enter(admit(access), today);
    }

    /**
     * Admits every holder of the access, or nobody if any condition fails.
     *
     * @return the number of users admitted
     */
    public int enter(T access, LocalDate today) {
        if (!access.getDate().equals(today)) {
            throw new BusinessException("Ticket " + access.getId() + " is not valid today (" + SystemDate.format(today) + ")");
        }
        List<User> holders = access.getHolders();
        for (User holder : holders) {
            if (visitors.contains(holder)) {
                throw new BusinessException("User " + holder.getEmail() + " is already in attraction " + id);
            }
        }
        if (holders.size() > available()) {
            throw new BusinessException("Not enough capacity in attraction " + id + ": " + available()
                    + " places left, " + holders.size() + " needed");
        }
        visitors.addAll(holders);
        return holders.size();
    }

    /**
     * Removes the holders of the access that are inside the attraction.
     *
     * @return the number of users that left
     */
    public int exit(Accessible access) {
        List<User> leaving = new ArrayList<>(access.getHolders());
        leaving.retainAll(visitors);
        if (leaving.isEmpty()) {
            throw new BusinessException("No user of ticket " + access.getId() + " is in attraction " + id);
        }
        leaving.forEach(visitors::remove);
        return leaving.size();
    }

    private static String kindOf(Accessible access) {
        if (access instanceof GroupAccess) {
            return "group ticket";
        }
        return access instanceof IndividualAccess ? "individual ticket" : "ticket";
    }
}
