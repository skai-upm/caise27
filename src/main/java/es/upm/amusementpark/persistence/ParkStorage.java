package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.User;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persistence of the whole park state through one repository per element
 * type. Normal and temporary modes differ only in the configured directory.
 */
public class ParkStorage {

    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;

    public ParkStorage(Path directory) {
        this.users = new UserRepository(directory.resolve("users.txt"));
        this.groups = new GroupRepository(directory.resolve("groups.txt"));
        this.tickets = new TicketRepository(directory.resolve("tickets.txt"));
        this.attractions = new AttractionRepository(directory.resolve("attractions.txt"));
    }

    /**
     * Rebuilds the stored state into the given park.
     *
     * @return lines that could not be read and were skipped
     */
    public List<String> load(ParkController park) {
        List<String> skipped = new ArrayList<>();
        for (User user : users.loadAll(skipped)) {
            park.registerUser(user);
        }
        groups.loadInto(park, skipped);
        attractions.loadInto(park, skipped);
        tickets.loadInto(park, skipped);
        return skipped;
    }

    public void save(ParkController park) {
        users.saveAll(park.getUsers());
        groups.saveAll(park.getGroups());
        attractions.saveAll(park.getAttractions());
        tickets.saveAll(park.getTickets());
    }
}
