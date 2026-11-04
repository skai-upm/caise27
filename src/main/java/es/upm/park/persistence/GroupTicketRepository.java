package es.upm.park.persistence;

import es.upm.park.business.model.Group;
import es.upm.park.business.model.GroupTicket;
import es.upm.park.business.model.User;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Storage of group tickets, with historical records that can no longer be linked. */
public interface GroupTicketRepository {

    GroupTicketFileStore.Contents load(Function<String, Optional<Group>> groupById,
                                       Function<String, Optional<User>> userByDni) throws IOException;

    void save(List<GroupTicket> groupTickets, List<String> historicalLines) throws IOException;
}
