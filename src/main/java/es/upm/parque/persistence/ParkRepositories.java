package es.upm.parque.persistence;

import java.util.Objects;

/**
 * The repositories of one park execution, configured during application initialization and injected into the
 * business layer. Repositories are replaceable dependencies (not singletons): the same controllers work with an
 * in-memory configuration or with one backed by the data files of any directory.
 */
public final class ParkRepositories {

    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;
    private final PassRepository passes;
    private final ParkFileStore store;

    private ParkRepositories(UserRepository users, ParkFileStore store) {
        this.users = Objects.requireNonNull(users);
        this.groups = new GroupRepository();
        this.tickets = new TicketRepository();
        this.attractions = new AttractionRepository();
        this.passes = new PassRepository();
        this.store = store;
    }

    /** Empty repositories kept only in memory. */
    public static ParkRepositories inMemory() {
        return inMemory(new UserRepository());
    }

    /** The given users plus empty in-memory group, ticket and attraction repositories. */
    public static ParkRepositories inMemory(UserRepository users) {
        return new ParkRepositories(users, null);
    }

    /** Repositories loaded from, and saved to, the given park file store. */
    public static ParkRepositories fileBacked(UserRepository users, ParkFileStore store) {
        ParkRepositories repositories = new ParkRepositories(users, Objects.requireNonNull(store));
        store.load(users, repositories.groups, repositories.tickets, repositories.attractions);
        return repositories;
    }

    public UserRepository getUsers() {
        return users;
    }

    public GroupRepository getGroups() {
        return groups;
    }

    public TicketRepository getTickets() {
        return tickets;
    }

    public AttractionRepository getAttractions() {
        return attractions;
    }

    /** Passes of the execution; they are kept in memory (file persistence of passes arrives with F3-C09). */
    public PassRepository getPasses() {
        return passes;
    }

    /** Saves groups, tickets and attractions when the configuration is backed by files. */
    public void save() {
        if (store != null) {
            store.save(groups, tickets, attractions);
        }
    }

    /** Park data lines ignored while loading; 0 for in-memory configurations. */
    public int getMalformedLineCount() {
        return store == null ? 0 : store.getMalformedLineCount();
    }
}
