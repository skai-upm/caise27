package es.upm.etsisi.amusementpark.business.attraction;

import es.upm.etsisi.amusementpark.business.ParkException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;

import java.util.List;

/**
 * A park attraction as seen by controllers and repositories. Which tickets it
 * accepts is fixed by its typed implementation.
 */
public interface Attraction {

    String getIdentifier();

    String getName();

    int getCapacity();

    AttractionType getType();

    List<User> getOccupants();

    int getAvailablePlaces();

    boolean isInside(User user);

    /**
     * Admits the ticket's holders and consumes the passes the attraction
     * requires, or changes nothing and throws.
     */
    EntryResult enter(Ticket ticket, boolean speed, PassLookup passes) throws ParkException;

    void release(User user);
}
