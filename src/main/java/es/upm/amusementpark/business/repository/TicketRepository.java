package es.upm.amusementpark.business.repository;

import es.upm.amusementpark.business.model.Group;
import es.upm.amusementpark.business.model.GroupTicket;
import es.upm.amusementpark.business.model.IndividualTicket;
import es.upm.amusementpark.business.model.User;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/** Storage of the ticket aggregate: stand-alone tickets and group tickets with their constituents. */
public interface TicketRepository {

    /** Tickets loaded from storage. */
    record Tickets(List<IndividualTicket> individual, List<GroupTicket> group) {
    }

    Tickets loadAll(Function<String, User> userByDni, Function<String, Group> groupById) throws IOException;

    void saveAll(List<IndividualTicket> individual, List<GroupTicket> group) throws IOException;
}
