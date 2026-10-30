package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.Ticket;
import es.upm.amusementpark.business.model.User;
import java.util.List;

/**
 * Every ticket, in one identifier space that includes the tickets composing group tickets.
 */
public interface TicketRepository {

    /**
     * Top-level tickets (individual tickets bought alone and group tickets).
     */
    List<Ticket> findAll();

    /**
     * Any ticket by identifier, including individual tickets composing a group ticket.
     */
    Ticket findById(String id);

    boolean exists(String id);

    boolean isComposed(Ticket ticket);

    /**
     * Every individual ticket (bought alone or composing a group ticket) held by the user.
     */
    List<IndividualTicket> ticketsOf(User user);

    List<GroupTicket> groupTicketsOf(Group group);

    void add(Ticket ticket);

    void remove(Ticket ticket);

    /**
     * Persists changes made to tickets already held by the repository.
     */
    void save();
}
