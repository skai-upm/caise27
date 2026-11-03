package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Group;
import es.upm.etsisi.amusementpark.business.model.GroupTicket;
import es.upm.etsisi.amusementpark.business.model.User;
import java.util.List;

/**
 * Repository of group tickets. Implementations decide where and how the data is stored; controllers depend only
 * on this interface, so the implementation or its location can be replaced at startup.
 */
public interface GroupTicketRepository {

    /** Loads the stored group tickets, linking them to the given groups and users. */
    List<GroupTicket> load(List<User> users, List<Group> groups);

    /** Stores exactly the given group tickets. */
    void save(List<GroupTicket> groupTickets);
}
