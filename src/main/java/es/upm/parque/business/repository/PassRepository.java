package es.upm.parque.business.repository;

import es.upm.parque.business.model.IndividualTicket;

import java.util.List;
import java.util.function.Function;

/**
 * Storage of the passes attached to individual tickets.
 */
public interface PassRepository {

    /** Reads the stored passes and attaches each one to its ticket, found through {@code ticketById}. */
    void load(Function<String, IndividualTicket> ticketById);

    /** Stores every pass of the given tickets. */
    void save(List<IndividualTicket> tickets);
}
