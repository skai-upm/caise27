package es.upm.park.persistence;

import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Storage of individually bought tickets, with historical records of deleted users. */
public interface TicketRepository {

    TicketFileStore.Contents load(Function<String, Optional<User>> userByDni) throws IOException;

    void save(List<IndividualTicket> tickets, List<String> historicalLines) throws IOException;
}
