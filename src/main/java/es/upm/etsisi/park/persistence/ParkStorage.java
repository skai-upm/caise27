package es.upm.etsisi.park.persistence;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * The set of repositories used by one execution, all rooted in the same data directory.
 * Normal and temporary (--fly) mode differ only in the directory given here.
 */
public class ParkStorage {

    public static final String USERS_FILE = "users.txt";
    public static final String GROUPS_FILE = "groups.txt";
    public static final String TICKETS_FILE = "tickets.txt";
    public static final String ATTRACTIONS_FILE = "attractions.txt";

    private final UserRepository users;
    private final GroupFileRepository groups;
    private final TicketFileRepository tickets;
    private final AttractionFileRepository attractions;

    public ParkStorage(UserRepository users, GroupFileRepository groups, TicketFileRepository tickets,
                       AttractionFileRepository attractions) {
        this.users = users;
        this.groups = groups;
        this.tickets = tickets;
        this.attractions = attractions;
    }

    public static ParkStorage inDirectory(Path directory, PrintStream warnings) {
        return new ParkStorage(
                new UserFileRepository(directory.resolve(USERS_FILE), warnings),
                new GroupFileRepository(directory.resolve(GROUPS_FILE), warnings),
                new TicketFileRepository(directory.resolve(TICKETS_FILE), warnings),
                new AttractionFileRepository(directory.resolve(ATTRACTIONS_FILE), warnings));
    }

    public UserRepository users() {
        return users;
    }

    public GroupFileRepository groups() {
        return groups;
    }

    public TicketFileRepository tickets() {
        return tickets;
    }

    public AttractionFileRepository attractions() {
        return attractions;
    }
}
