package es.upm.etsisi.park;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Aggregates the repositories of every persistent park element, all located in one directory.
 * Normal and {@code --fly} executions differ only in that directory.
 */
public class ParkRepository {

    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;

    public ParkRepository(Path directory) {
        this.users = new UserRepository(directory.resolve("users.txt"));
        this.groups = new GroupRepository(directory.resolve("groups.txt"));
        this.tickets = new TicketRepository(directory.resolve("tickets.txt"));
        this.attractions = new AttractionRepository(directory.resolve("attractions.txt"));
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

    public void save(Collection<User> userList, Collection<Group> groupList,
                     Collection<Ticket> ticketList, Collection<Attraction> attractionList) {
        Map<Path, java.util.List<String>> contents = new LinkedHashMap<>();
        contents.put(users.getFile(), users.toLines(userList));
        contents.put(groups.getFile(), groups.toLines(groupList));
        contents.put(tickets.getFile(), tickets.toLines(ticketList));
        contents.put(attractions.getFile(), attractions.toLines(attractionList));
        TextFile.writeAll(contents);
    }
}
