package es.upm.park.persistence.repository;

import java.nio.file.Path;

import es.upm.park.persistence.AttractionFileStore;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.persistence.PassFileStore;
import es.upm.park.persistence.TicketFileStore;
import es.upm.park.persistence.UserFileStore;

/**
 * Repository configuration created during application initialization and
 * injected into the controllers. Repositories are ordinary, replaceable
 * instances: each configuration has its own.
 */
public record ParkRepositories(UserRepository users, GroupRepository groups, AttractionRepository attractions,
        TicketRepository tickets, PassRepository passes) {

    public static final String USERS_FILE = "users.txt";
    public static final String GROUPS_FILE = "groups.txt";
    public static final String ATTRACTIONS_FILE = "attractions.txt";
    public static final String TICKETS_FILE = "tickets.txt";
    public static final String PASSES_FILE = "passes.txt";

    /** Repositories without persistence. */
    public static ParkRepositories inMemory() {
        return inMemory(new UserRepository());
    }

    /** In-memory repositories around the given users. */
    public static ParkRepositories inMemory(UserRepository users) {
        return new ParkRepositories(users, new GroupRepository(), new AttractionRepository(), new TicketRepository(),
                new PassRepository());
    }

    /**
     * Repositories backed by the text files of {@code dataDir}, loaded in
     * dependency order: users, groups, attractions, tickets, passes.
     *
     * @throws es.upm.park.persistence.PersistenceException when stored data cannot be read
     */
    public static ParkRepositories load(Path dataDir) {
        UserRepository users = new UserRepository(new UserFileStore(dataDir.resolve(USERS_FILE)));
        GroupRepository groups = new GroupRepository(new GroupFileStore(dataDir.resolve(GROUPS_FILE)),
                users::findByDni);
        AttractionRepository attractions =
                new AttractionRepository(new AttractionFileStore(dataDir.resolve(ATTRACTIONS_FILE)));
        TicketRepository tickets = new TicketRepository(new TicketFileStore(dataDir.resolve(TICKETS_FILE)),
                users::findByDni, groups::findById);
        PassRepository passes = new PassRepository(new PassFileStore(dataDir.resolve(PASSES_FILE)),
                tickets::findIndividual);
        return new ParkRepositories(users, groups, attractions, tickets, passes);
    }
}
