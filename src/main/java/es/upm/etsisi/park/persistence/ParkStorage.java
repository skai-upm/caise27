package es.upm.etsisi.park.persistence;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * The set of repositories used by the park, all located in one data
 * directory. Normal and temporary (--fly) executions only differ in that
 * directory.
 */
public class ParkStorage {

    public static final String DATA_DIR_PROPERTY = "park.data.dir";
    public static final String DEFAULT_DATA_DIR = ".";

    private final Path dataDirectory;
    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;

    public ParkStorage(Path dataDirectory, PrintStream warnings) {
        this.dataDirectory = dataDirectory;
        this.users = new UserRepository(dataDirectory.resolve(UserRepository.FILE_NAME), warnings);
        this.groups = new GroupRepository(dataDirectory.resolve(GroupRepository.FILE_NAME), warnings);
        this.tickets = new TicketRepository(dataDirectory.resolve(TicketRepository.FILE_NAME), warnings);
        this.attractions = new AttractionRepository(dataDirectory.resolve(AttractionRepository.FILE_NAME), warnings);
    }

    /** Storage in the directory given by {@value #DATA_DIR_PROPERTY}, or the working directory. */
    public static ParkStorage fromSystemProperty(PrintStream warnings) {
        return new ParkStorage(Path.of(System.getProperty(DATA_DIR_PROPERTY, DEFAULT_DATA_DIR)), warnings);
    }

    public Path getDataDirectory() {
        return dataDirectory;
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
}
