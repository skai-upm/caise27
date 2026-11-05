package es.upm.amusementpark.persistence.repository;

import es.upm.amusementpark.persistence.storage.AttractionFileStorage;
import es.upm.amusementpark.persistence.storage.GroupFileStorage;
import es.upm.amusementpark.persistence.storage.PassFileStorage;
import es.upm.amusementpark.persistence.storage.TicketFileStorage;
import es.upm.amusementpark.persistence.storage.UserFileStorage;
import java.nio.file.Path;

/**
 * The set of repositories used by one execution. Created during initialization and injected;
 * repositories are replaceable dependencies, not singletons.
 */
public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                           AttractionRepository attractions, PassRepository passes) {

    /** File-backed repositories in {@code dataDirectory}, loaded in dependency order. */
    public static Repositories files(Path dataDirectory) {
        UserRepository u = new UserRepository(new UserFileStorage(dataDirectory.resolve("users.txt")));
        GroupRepository g = new GroupRepository(new GroupFileStorage(dataDirectory.resolve("groups.txt")), u);
        TicketRepository t = new TicketRepository(new TicketFileStorage(dataDirectory.resolve("tickets.txt")), u, g);
        AttractionRepository a = new AttractionRepository(new AttractionFileStorage(dataDirectory.resolve("attractions.txt")));
        PassRepository p = new PassRepository(new PassFileStorage(dataDirectory.resolve("passes.txt")), t);
        return new Repositories(u, g, t, a, p);
    }

    /** In-memory repositories around the given users repository. */
    public static Repositories inMemory(UserRepository users) {
        return new Repositories(users, new GroupRepository(), new TicketRepository(), new AttractionRepository(),
                new PassRepository());
    }

    /** Empty in-memory repositories. */
    public static Repositories inMemory() {
        return inMemory(new UserRepository());
    }
}
