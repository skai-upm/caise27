package es.upm.etsisi.amusementpark.persistence;

import java.nio.file.Path;

/**
 * The set of repositories an execution works with. Created at start-up and injected where needed.
 */
public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                           AttractionRepository attractions, PassRepository passes) {

    /**
     * File-based repositories whose files live in the given directory.
     */
    public static Repositories fileBased(Path dataDir) {
        UserRepository users = new UserRepository(new UserFileStore(dataDir.resolve("users.txt")));
        AttractionRepository attractions =
                new AttractionRepository(new AttractionFileStore(dataDir.resolve("attractions.txt")));
        GroupRepository groups = new GroupRepository(new GroupFileStore(dataDir.resolve("groups.txt"), users));
        TicketRepository tickets =
                new TicketRepository(new TicketFileStore(dataDir.resolve("tickets.txt"), users, groups));
        PassRepository passes = new PassRepository(new PassFileStore(dataDir.resolve("passes.txt"), tickets));
        return new Repositories(users, groups, tickets, attractions, passes);
    }
}
