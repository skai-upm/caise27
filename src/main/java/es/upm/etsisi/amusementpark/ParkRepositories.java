package es.upm.etsisi.amusementpark;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The park's repositories, all configured from one base directory. Normal and
 * temporary (--fly) executions differ only in that directory.
 */
public class ParkRepositories {

    /** Everything the park persists. */
    public record ParkData(List<User> users, List<Group> groups, List<Ticket> tickets, List<Attraction> attractions) {
    }

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TicketRepository ticketRepository;
    private final AttractionRepository attractionRepository;

    public ParkRepositories(Path baseDirectory, PrintStream warnings) {
        userRepository = new UserRepository(baseDirectory.resolve("users.txt"), warnings);
        groupRepository = new GroupRepository(baseDirectory.resolve("groups.txt"), warnings);
        ticketRepository = new TicketRepository(baseDirectory.resolve("tickets.txt"), warnings);
        attractionRepository = new AttractionRepository(baseDirectory.resolve("attractions.txt"), warnings);
    }

    public ParkData load() throws ParkException {
        List<User> users = userRepository.load();
        Map<String, User> usersByDni = new HashMap<>();
        for (User user : users) {
            usersByDni.put(user.getDni(), user);
        }
        List<Group> groups = groupRepository.load(usersByDni);
        Map<String, Group> groupsById = new HashMap<>();
        for (Group group : groups) {
            groupsById.put(group.getIdentifier(), group);
        }
        List<Ticket> tickets = ticketRepository.load(usersByDni, groupsById);
        return new ParkData(users, groups, tickets, attractionRepository.load());
    }

    public void save(ParkData data) throws ParkException {
        userRepository.save(data.users());
        groupRepository.save(data.groups());
        ticketRepository.save(data.tickets());
        attractionRepository.save(data.attractions());
    }
}
