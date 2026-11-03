package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Ticket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;

/**
 * Repository of tickets. Implementations decide where and how the data is stored; controllers depend only
 * on this interface, so the implementation or its location can be replaced at startup.
 */
public interface TicketRepository {

    /** Loads the stored individual tickets, linking owners to the given users. */
    List<Ticket> load(List<User> users);

    /** Stores exactly the given individual tickets. */
    void save(List<Ticket> tickets);
}
