package es.upm.amusementpark.persistence;

import es.upm.amusementpark.park.ParkController;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Configures every repository with a file inside one data directory. Normal and temporary ({@code --fly})
 * persistence differ only in that directory.
 */
public class ParkStorage {

    private final Path directory;
    private final AttractionRepository attractions;
    private final GroupRepository groups;
    private final TicketRepository tickets;

    public ParkStorage(Path directory) {
        this.directory = directory;
        this.attractions = new AttractionRepository(directory.resolve("attractions.txt"));
        this.groups = new GroupRepository(directory.resolve("groups.txt"));
        this.tickets = new TicketRepository(directory.resolve("tickets.txt"));
    }

    public Path getDirectory() {
        return directory;
    }

    /**
     * Rebuilds the persisted state into {@code park} (attractions, groups, then tickets).
     *
     * @return warnings about records that could not be interpreted
     */
    public List<String> load(ParkController park) throws IOException {
        List<String> warnings = new ArrayList<>();
        attractions.load(park, warnings);
        groups.load(park, warnings);
        tickets.load(park, warnings);
        return warnings;
    }

    /** Stores the current state of {@code park}. */
    public void save(ParkController park) throws IOException {
        attractions.save(park);
        groups.save(park);
        tickets.save(park);
    }
}
