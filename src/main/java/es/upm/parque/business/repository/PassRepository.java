package es.upm.parque.business.repository;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;

/**
 * Storage of personal passes. Loaded passes are attached to their individual tickets.
 */
public interface PassRepository {

    List<Pass> load(Function<String, IndividualTicket> ticketById);

    /** Stores every pass owned by the given tickets. */
    void save(Collection<IndividualTicket> tickets);
}
