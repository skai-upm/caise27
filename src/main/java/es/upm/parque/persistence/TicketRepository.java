package es.upm.parque.persistence;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;

/**
 * Storage abstraction for tickets. Implementations decide where and how data is kept.
 */
public interface TicketRepository {

    /**
     * Loads the stored tickets. Holders are resolved by DNI; a DNI of a user that no longer exists
     * becomes a detached historical holder so past tickets are kept.
     */
    List<IndividualTicket> load(Function<String, User> userByDni) throws IOException;

    void save(Collection<IndividualTicket> tickets) throws IOException;
}
