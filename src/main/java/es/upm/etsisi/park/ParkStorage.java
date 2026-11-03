package es.upm.etsisi.park;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * The park's repositories, all located in one data directory. Normal and
 * temporary (--fly) executions differ only in that directory.
 */
public class ParkStorage {

    private final Path directory;
    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;

    public ParkStorage(Path directory) {
        this.directory = directory;
        this.users = new UserRepository(directory.resolve("users.txt"));
        this.groups = new GroupRepository(directory.resolve("groups.txt"));
        this.tickets = new TicketRepository(directory.resolve("tickets.txt"));
        this.attractions = new AttractionRepository(directory.resolve("attractions.txt"));
    }

    public Path getDirectory() {
        return directory;
    }

    public UserRepository users() {
        return users;
    }

    public GroupRepository groups() {
        return groups;
    }

    public TicketRepository tickets() {
        return tickets;
    }

    public AttractionRepository attractions() {
        return attractions;
    }

    public void saveAll(Collection<User> allUsers, Collection<Group> allGroups,
                        Collection<Ticket> allTickets, Collection<Attraction> allAttractions) throws IOException {
        users.saveAll(allUsers);
        groups.saveAll(allGroups);
        tickets.saveAll(allTickets);
        attractions.saveAll(allAttractions);
    }
}
