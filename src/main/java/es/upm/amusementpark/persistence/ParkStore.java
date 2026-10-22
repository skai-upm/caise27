package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Attraction;
import es.upm.amusementpark.model.Group;
import es.upm.amusementpark.model.Ticket;
import es.upm.amusementpark.model.User;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * All persistent park data inside one data directory. Normal and temporary
 * (--fly) executions use this same class with different directories.
 */
public class ParkStore {

    private final Path directory;
    private final FileUserRepository users;
    private final FileGroupRepository groups;
    private final FileTicketRepository tickets;
    private final FileAttractionRepository attractions;

    public ParkStore(Path directory, PrintStream warnings) {
        this.directory = directory;
        this.users = new FileUserRepository(directory.resolve(FileUserRepository.DEFAULT_FILE_NAME), warnings);
        this.groups = new FileGroupRepository(directory.resolve(FileGroupRepository.DEFAULT_FILE_NAME), warnings);
        this.tickets = new FileTicketRepository(directory.resolve(FileTicketRepository.DEFAULT_FILE_NAME), warnings);
        this.attractions = new FileAttractionRepository(
                directory.resolve(FileAttractionRepository.DEFAULT_FILE_NAME), warnings);
    }

    public Path getDirectory() {
        return directory;
    }

    public List<User> loadUsers() throws IOException {
        return users.loadAll();
    }

    public List<Group> loadGroups(Function<String, User> userByDni) throws IOException {
        return groups.load(userByDni);
    }

    public List<Ticket> loadTickets(Function<String, User> userByDni, Function<String, Group> groupById)
            throws IOException {
        return tickets.load(userByDni, groupById);
    }

    public List<Attraction> loadAttractions() throws IOException {
        return attractions.load();
    }

    /** Writes every data file; if any write fails, no file is changed. */
    public void saveAll(ParkData data) throws IOException {
        Map<Path, List<String>> contents = new LinkedHashMap<>();
        contents.put(users.getFile(), FileUserRepository.toLines(data.users()));
        contents.put(groups.getFile(), FileGroupRepository.toLines(data.groups()));
        contents.put(tickets.getFile(), FileTicketRepository.toLines(data.tickets()));
        contents.put(attractions.getFile(), FileAttractionRepository.toLines(data.attractions()));
        TextFiles.writeAll(contents);
    }

    /** The complete park state to persist. */
    public record ParkData(Collection<User> users, Collection<Group> groups, Collection<Ticket> tickets,
                           Collection<Attraction> attractions) {
    }
}
