package es.upm.parque.business.repository;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.Pass;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Storage of personal passes; tickets are resolved through an identifier lookup.
 */
public interface PassRepository {

    List<Pass> load(Function<String, IndividualTicket> ticketsById);

    void saveAll(Collection<Pass> passes);
}
