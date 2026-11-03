package es.upm.etsisi.amusementpark.business.repository;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.ticket.IndividualTicket;
import es.upm.etsisi.amusementpark.business.ticket.Pass;

import java.util.List;
import java.util.Map;

/**
 * Stores and retrieves passes, linking them to already loaded individual tickets.
 */
public interface PassRepository {

    List<Pass> load(Map<String, IndividualTicket> ticketsById) throws StorageException;

    void save(List<Pass> passes) throws StorageException;
}
