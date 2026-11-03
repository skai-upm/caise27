package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.User;
import es.upm.etsisi.amusementpark.business.group.Group;
import es.upm.etsisi.amusementpark.business.ticket.Ticket;

import java.util.List;
import java.util.Map;

/**
 * Stores and retrieves tickets, linking them to already loaded users and groups.
 */
public interface TicketRepository {

    List<Ticket> load(Map<String, User> usersByDni, Map<String, Group> groupsById) throws StorageException;

    void save(List<Ticket> tickets) throws StorageException;
}
