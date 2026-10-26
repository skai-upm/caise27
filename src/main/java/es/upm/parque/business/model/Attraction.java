package es.upm.parque.business.model;

import es.upm.parque.business.BusinessException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A park attraction with a maximum capacity and the users currently inside it.
 * Entry and exit work with any kind of ticket through {@link Ticket#getHolders()}.
 */
public class Attraction {

    private final String id;
    private final String name;
    private final int capacity;
    private final Set<User> visitors = new LinkedHashSet<>();

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

    public List<User> getVisitors() {
        return List.copyOf(visitors);
    }

    public int available() {
        return capacity - visitors.size();
    }

    /**
     * Admits every holder of the ticket, or nobody if any condition fails.
     *
     * @return the number of users admitted
     */
    public int enter(Ticket ticket, LocalDate today) {
        if (!ticket.getDate().equals(today)) {
            throw new BusinessException("Ticket " + ticket.getId() + " is not valid today (" + SystemDate.format(today) + ")");
        }
        List<User> holders = ticket.getHolders();
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
     * Removes the ticket's holders that are inside the attraction.
     *
     * @return the number of users that left
     */
    public int exit(Ticket ticket) {
        List<User> leaving = new ArrayList<>(ticket.getHolders());
        leaving.retainAll(visitors);
        if (leaving.isEmpty()) {
            throw new BusinessException("No user of ticket " + ticket.getId() + " is in attraction " + id);
        }
        leaving.forEach(visitors::remove);
        return leaving.size();
    }
}
