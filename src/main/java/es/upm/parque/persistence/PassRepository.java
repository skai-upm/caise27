package es.upm.parque.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Function;

import es.upm.parque.business.model.IndividualTicket;

/**
 * Storage abstraction for personal passes. Implementations decide where and how data is kept.
 */
public interface PassRepository {

    /**
     * Loads the stored passes and attaches each one to its ticket, found through the lookup.
     */
    void load(Function<String, IndividualTicket> ticketById) throws IOException;

    /**
     * Stores every pass of the given tickets.
     */
    void save(Collection<IndividualTicket> tickets) throws IOException;
}
