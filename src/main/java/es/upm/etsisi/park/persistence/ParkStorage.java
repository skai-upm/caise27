package es.upm.etsisi.park.persistence;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * The set of repositories used by one execution. Normal and temporary (--fly) mode differ only
 * in the directory given to {@link #inDirectory}; other implementations can be supplied directly.
 */
public class ParkStorage {

    public static final String USERS_FILE = "users.txt";
    public static final String GROUPS_FILE = "groups.txt";
    public static final String TICKETS_FILE = "tickets.txt";
    public static final String PASSES_FILE = "passes.txt";
    public static final String ATTRACTIONS_FILE = "attractions.txt";

    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final PassRepository passes;
    private final AttractionRepository attractions;

    public ParkStorage(UserRepository users, GroupRepository groups, TicketRepository tickets,
                       PassRepository passes, AttractionRepository attractions) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.passes = passes;
        this.attractions = attractions;
    }

    public static ParkStorage inDirectory(Path directory, PrintStream warnings) {
        return new ParkStorage(
                new UserFileRepository(directory.resolve(USERS_FILE), warnings),
                new GroupFileRepository(directory.resolve(GROUPS_FILE), warnings),
                new TicketFileRepository(directory.resolve(TICKETS_FILE), warnings),
                new PassFileRepository(directory.resolve(PASSES_FILE), warnings),
                new AttractionFileRepository(directory.resolve(ATTRACTIONS_FILE), warnings));
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

    public PassRepository passes() {
        return passes;
    }

    public AttractionRepository attractions() {
        return attractions;
    }
}
