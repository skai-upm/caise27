package es.upm.amusementpark.persistence;

import java.nio.file.Path;

/**
 * The repositories of one park configuration, created during application
 * initialization and injected into the controllers. Repositories are ordinary
 * objects, not singletons, so different configurations can coexist.
 *
 * @param ignoredLines number of malformed stored lines skipped while loading
 */
public record ParkRepositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
        AttractionRepository attractions, PassRepository passes, int ignoredLines) {

    static final String USERS_FILE = "users.txt";
    static final String GROUPS_FILE = "groups.txt";
    static final String TICKETS_FILE = "tickets.txt";
    static final String ATTRACTIONS_FILE = "attractions.txt";

    /** Repositories kept in memory only. */
    public static ParkRepositories inMemory() {
        return new ParkRepositories(new UserRepository(), new GroupRepository(), new TicketRepository(),
                new AttractionRepository(), new PassRepository(), 0);
    }

    /**
     * Repositories stored in text files inside the given directory, loaded
     * from them.
     */
    public static ParkRepositories fromDirectory(Path dataDir) {
        UserFileStorage userStorage = new UserFileStorage(dataDir.resolve(USERS_FILE));
        UserRepository users = new UserRepository(userStorage);
        GroupFileStorage groupStorage = new GroupFileStorage(dataDir.resolve(GROUPS_FILE));
        GroupRepository groups = new GroupRepository(groupStorage, users);
        TicketFileStorage ticketStorage = new TicketFileStorage(dataDir.resolve(TICKETS_FILE));
        TicketRepository tickets = new TicketRepository(ticketStorage, users, groups);
        AttractionFileStorage attractionStorage = new AttractionFileStorage(dataDir.resolve(ATTRACTIONS_FILE));
        AttractionRepository attractions = new AttractionRepository(attractionStorage);
        int ignored = userStorage.getSkippedLines() + groupStorage.getSkippedLines()
                + ticketStorage.getSkippedLines() + attractionStorage.getSkippedLines();
        return new ParkRepositories(users, groups, tickets, attractions, new PassRepository(), ignored);
    }
}
