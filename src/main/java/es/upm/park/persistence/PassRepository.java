package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.pass.Pass;

import java.util.List;
import java.util.function.Function;

/**
 * Storage of SpeedPasses and Special Passes with their price and state.
 */
public interface PassRepository {

    /**
     * @param ticketById resolves individual tickets, standalone or composing a group ticket
     */
    List<Pass> loadAll(Function<String, IndividualTicket> ticketById);

    void saveAll(Iterable<Pass> passes);
}
