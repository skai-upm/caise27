package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.util.List;

/**
 * Storage of standalone individual tickets and group tickets with their constituent tickets.
 */
public interface TicketRepository {

    List<IndividualTicket> loadTickets(List<User> users) throws IOException;

    List<GroupTicket> loadGroupTickets(List<User> users, List<Group> groups) throws IOException;

    void save(List<IndividualTicket> tickets, List<GroupTicket> groupTickets) throws IOException;
}
