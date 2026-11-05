package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.ParkController;
import es.upm.amusementpark.business.ParkRepository;
import es.upm.amusementpark.business.RepositoryException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Text-file implementation of {@link ParkRepository}. Its element repositories are injected; normal and temporary
 * ({@code --fly}) persistence differ only in the directory they are configured with.
 */
public class ParkStorage implements ParkRepository {

    private final Path directory;
    private final AttractionRepository attractions;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final PassRepository passes;

    public ParkStorage(Path directory, AttractionRepository attractions, GroupRepository groups,
            TicketRepository tickets, PassRepository passes) {
        this.directory = Objects.requireNonNull(directory, "directory");
        this.attractions = Objects.requireNonNull(attractions, "attractions");
        this.groups = Objects.requireNonNull(groups, "groups");
        this.tickets = Objects.requireNonNull(tickets, "tickets");
        this.passes = Objects.requireNonNull(passes, "passes");
    }

    /**
     * Configuration used at application initialization: repositories with their files inside {@code directory}.
     *
     * @param directory data directory
     * @return a new, independent storage
     */
    public static ParkStorage inDirectory(Path directory) {
        return new ParkStorage(directory, new AttractionRepository(directory.resolve("attractions.txt")),
                new GroupRepository(directory.resolve("groups.txt")),
                new TicketRepository(directory.resolve("tickets.txt")),
                new PassRepository(directory.resolve("passes.txt")));
    }

    public Path getDirectory() {
        return directory;
    }

    /** Rebuilds attractions, groups, tickets, then passes. */
    @Override
    public List<String> load(ParkController park) {
        List<String> warnings = new ArrayList<>();
        try {
            attractions.load(park, warnings);
            groups.load(park, warnings);
            tickets.load(park, warnings);
            passes.load(park, warnings);
        } catch (IOException e) {
            throw new RepositoryException("Cannot read persisted data from " + directory, e);
        }
        return warnings;
    }

    @Override
    public void save(ParkController park) {
        try {
            attractions.save(park);
            groups.save(park);
            tickets.save(park);
            passes.save(park);
        } catch (IOException e) {
            throw new RepositoryException("Cannot save data to " + directory, e);
        }
    }
}
