package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Pass;
import es.upm.etsisi.amusementpark.business.model.Ticket;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Repository of personal passes. Implementations decide where and how the data is stored; controllers depend
 * only on this interface, so the implementation or its location can be replaced at startup.
 */
public interface PassRepository {

    /** Loads the stored passes, linking each to the individual ticket returned by {@code ticketById}. */
    List<Pass> load(Function<String, Optional<Ticket>> ticketById);

    /** Stores exactly the given passes. */
    void save(List<Pass> passes);
}
