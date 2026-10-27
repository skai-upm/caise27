package es.upm.amusementpark.business.controller;

import java.nio.file.Path;

import es.upm.amusementpark.business.invoice.InvoiceController;
import es.upm.amusementpark.business.invoice.StandardInvoiceBuilder;
import es.upm.amusementpark.business.security.BCryptPasswordHasher;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.AttractionFileStore;
import es.upm.amusementpark.persistence.DataPaths;
import es.upm.amusementpark.persistence.GroupFileStore;
import es.upm.amusementpark.persistence.TemporaryDataDirectory;
import es.upm.amusementpark.persistence.TicketFileStore;
import es.upm.amusementpark.persistence.UserFileStore;
import es.upm.amusementpark.persistence.repository.AttractionRepository;
import es.upm.amusementpark.persistence.repository.GroupRepository;
import es.upm.amusementpark.persistence.repository.PassRepository;
import es.upm.amusementpark.persistence.repository.TicketRepository;
import es.upm.amusementpark.persistence.repository.UserRepository;

/**
 * Composition root: creates the repositories for the execution mode and injects them,
 * together with the collaborations between controllers, into the singleton controllers.
 */
public final class ApplicationInitializer {

    private static TemporaryDataDirectory temporary;

    private ApplicationInitializer() {
    }

    /** Normal execution: data files in the given base directory. */
    public static void startPermanent(Path baseDir) {
        startWithFiles(new DataPaths(baseDir));
    }

    private static final int FILE_LOG_ROUNDS = 10;
    private static final int MEMORY_LOG_ROUNDS = 4;

    /** --fly execution: data files in a new temporary directory under {@code <base>/tmp}. */
    public static void startTemporary(Path baseDir) {
        temporary = TemporaryDataDirectory.create(baseDir);
        startWithFiles(new DataPaths(temporary.path()));
    }

    /** Execution without persistence, using the given user repository and in-memory repositories. */
    public static void startInMemory(UserRepository users) {
        inject(users, new GroupRepository(), new TicketRepository(), new AttractionRepository(),
                new BCryptPasswordHasher(MEMORY_LOG_ROUNDS));
    }

    /** Ends the execution, deleting the temporary data directory if one was created. */
    public static void close() {
        if (temporary != null) {
            TemporaryDataDirectory dir = temporary;
            temporary = null;
            dir.delete();
        }
    }

    private static void startWithFiles(DataPaths paths) {
        UserRepository users = new UserRepository(new UserFileStore(paths.users()));
        GroupRepository groups = new GroupRepository(new GroupFileStore(paths.groups()), users);
        TicketRepository tickets = new TicketRepository(new TicketFileStore(paths.tickets()), users, groups);
        AttractionRepository attractions = new AttractionRepository(new AttractionFileStore(paths.attractions()));
        inject(users, groups, tickets, attractions, new BCryptPasswordHasher(FILE_LOG_ROUNDS));
    }

    private static void inject(UserRepository users, GroupRepository groups, TicketRepository tickets,
            AttractionRepository attractions, PasswordHasher hasher) {
        PassRepository passes = new PassRepository();
        ParkController park = ParkController.getInstance();
        park.configure(users, groups, tickets, attractions);
        park.resetClock();
        UserController.getInstance().configure(users, park, hasher);
        GroupController.getInstance().configure(groups, users, park);
        PassController.getInstance().configure(passes, tickets, park);
        TicketController.getInstance().configure(tickets, users, groups, park, PassController.getInstance());
        AttractionController.getInstance().configure(attractions, tickets, passes, park);
        InvoiceController.getInstance().configure(users, groups, tickets, passes, new StandardInvoiceBuilder());
    }
}
