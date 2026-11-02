package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.Group;
import es.upm.etsisi.park.business.IndividualTicket;
import es.upm.etsisi.park.business.ParkData;
import es.upm.etsisi.park.business.ParkRepository;
import es.upm.etsisi.park.business.Ticket;
import es.upm.etsisi.park.business.User;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Text-file implementation of {@link ParkRepository}: one file per element kind, all in one
 * directory. Normal and {@code --fly} executions differ only in that directory.
 */
public class TextParkRepository implements ParkRepository {

    private final UserFileStore users;
    private final GroupFileStore groups;
    private final TicketFileStore tickets;
    private final PassFileStore passes;
    private final AttractionFileStore attractions;

    public TextParkRepository(Path directory) {
        this.users = new UserFileStore(directory.resolve("users.txt"));
        this.groups = new GroupFileStore(directory.resolve("groups.txt"));
        this.tickets = new TicketFileStore(directory.resolve("tickets.txt"));
        this.passes = new PassFileStore(directory.resolve("passes.txt"));
        this.attractions = new AttractionFileStore(directory.resolve("attractions.txt"));
    }

    @Override
    public ParkData load() {
        List<User> userList = users.load();
        Map<String, User> usersByDni = new LinkedHashMap<>();
        userList.forEach(u -> usersByDni.put(u.getDni(), u));
        List<Group> groupList = groups.load(usersByDni);
        Map<String, Group> groupsById = new LinkedHashMap<>();
        groupList.forEach(g -> groupsById.put(g.getId(), g));
        List<Ticket> ticketList = tickets.load(usersByDni, groupsById);
        Map<String, IndividualTicket> individualById = new LinkedHashMap<>();
        for (Ticket t : ticketList) {
            t.getEntryTickets().forEach(i -> individualById.put(i.getId(), i));
        }
        return new ParkData(userList, groupList, ticketList, passes.load(individualById), attractions.load());
    }

    @Override
    public void save(ParkData data) {
        Map<Path, List<String>> contents = new LinkedHashMap<>();
        contents.put(users.getFile(), users.toLines(data.users()));
        contents.put(groups.getFile(), groups.toLines(data.groups()));
        contents.put(tickets.getFile(), tickets.toLines(data.tickets()));
        contents.put(passes.getFile(), passes.toLines(data.passes()));
        contents.put(attractions.getFile(), attractions.toLines(data.attractions()));
        TextFile.writeAll(contents);
    }
}
