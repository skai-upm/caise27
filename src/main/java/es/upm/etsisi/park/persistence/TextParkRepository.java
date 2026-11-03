package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.Attraction;
import es.upm.etsisi.park.business.GeneralAccess;
import es.upm.etsisi.park.business.Group;
import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.ParkRepository;
import es.upm.etsisi.park.business.Pass;
import es.upm.etsisi.park.business.Ticket;
import es.upm.etsisi.park.business.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Text-file implementation of the park repository: one file per element type, all
 * in one data directory. Normal and temporary (--fly) executions differ only in
 * that directory.
 */
public class TextParkRepository implements ParkRepository {

    private final Path directory;
    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;
    private final PassRepository passes;

    public TextParkRepository(Path directory) {
        this.directory = directory;
        this.users = new UserRepository(directory.resolve("users.txt"));
        this.groups = new GroupRepository(directory.resolve("groups.txt"));
        this.tickets = new TicketRepository(directory.resolve("tickets.txt"));
        this.attractions = new AttractionRepository(directory.resolve("attractions.txt"));
        this.passes = new PassRepository(directory.resolve("passes.txt"));
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public List<User> loadUsers() throws IOException {
        return users.load();
    }

    @Override
    public List<Attraction<? extends GeneralAccess>> loadAttractions() throws IOException {
        return attractions.load();
    }

    @Override
    public List<Group> loadGroups(Map<String, User> usersByDni) throws IOException {
        return groups.load(usersByDni);
    }

    @Override
    public List<Ticket> loadTickets(Map<String, User> usersByDni, Map<String, Group> groupsById) throws IOException {
        return tickets.load(usersByDni, groupsById);
    }

    @Override
    public List<Pass> loadPasses(Function<String, IndividualTicket> individualTicketById) throws IOException {
        return passes.load(individualTicketById);
    }

    @Override
    public void saveAll(Collection<User> allUsers, Collection<Group> allGroups, Collection<Ticket> allTickets,
                        Collection<Attraction<? extends GeneralAccess>> allAttractions, Collection<Pass> allPasses)
            throws IOException {
        users.saveAll(allUsers);
        groups.saveAll(allGroups);
        tickets.saveAll(allTickets);
        attractions.saveAll(allAttractions);
        passes.saveAll(allPasses);
    }
}
