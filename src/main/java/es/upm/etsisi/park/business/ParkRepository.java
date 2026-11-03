package es.upm.etsisi.park.business;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Persistence port of the park. Implementations are created at start-up and
 * injected, so normal, temporary or alternative storage can be used without
 * changing business logic.
 */
public interface ParkRepository {

    List<User> loadUsers() throws IOException;

    List<Attraction<? extends GeneralAccess>> loadAttractions() throws IOException;

    List<Group> loadGroups(Map<String, User> usersByDni) throws IOException;

    List<Ticket> loadTickets(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException;

    List<Pass> loadPasses(Function<String, IndividualTicket> individualTicketById) throws IOException;

    void saveAll(Collection<User> users, Collection<Group> groups, Collection<Ticket> tickets,
                 Collection<Attraction<? extends GeneralAccess>> attractions, Collection<Pass> passes) throws IOException;
}
