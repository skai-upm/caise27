package es.upm.amusementpark;

import es.upm.amusementpark.business.controller.AttractionController;
import es.upm.amusementpark.business.controller.GroupController;
import es.upm.amusementpark.business.controller.InvoiceController;
import es.upm.amusementpark.business.controller.Park;
import es.upm.amusementpark.business.controller.PassController;
import es.upm.amusementpark.business.controller.SystemController;
import es.upm.amusementpark.business.controller.TicketController;
import es.upm.amusementpark.business.controller.UserController;
import es.upm.amusementpark.business.security.PasswordHasher;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.FileAttractionRepository;
import es.upm.amusementpark.persistence.FileDataStore;
import es.upm.amusementpark.persistence.FileGroupRepository;
import es.upm.amusementpark.persistence.FilePassRepository;
import es.upm.amusementpark.persistence.FileTicketRepository;
import es.upm.amusementpark.persistence.FileUserRepository;
import es.upm.amusementpark.persistence.GroupRepository;
import es.upm.amusementpark.persistence.PassRepository;
import es.upm.amusementpark.persistence.TicketRepository;
import es.upm.amusementpark.persistence.TransactionManager;
import es.upm.amusementpark.persistence.UserRepository;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Composition root: creates the repositories for a data directory and injects
 * them, together with the collaborations between controllers, into the
 * Singleton controllers.
 */
public final class ApplicationContext {

    private ApplicationContext() {
    }

    /** The replaceable persistence dependencies of the controllers. */
    public record Repositories(UserRepository users, GroupRepository groups, TicketRepository tickets,
                               AttractionRepository attractions, PassRepository passes,
                               TransactionManager transactions) {

        public Repositories withUsers(UserRepository replacement) {
            return new Repositories(replacement, groups, tickets, attractions, passes, transactions);
        }
    }

    /** File repositories storing their data inside {@code dataDir}. */
    public static Repositories fileRepositories(Path dataDir, PrintStream warnings) {
        FileDataStore store = new FileDataStore();
        return new Repositories(
                new FileUserRepository(dataDir.resolve(FileUserRepository.DEFAULT_FILE_NAME), store, warnings),
                new FileGroupRepository(dataDir.resolve(FileGroupRepository.DEFAULT_FILE_NAME), store, warnings),
                new FileTicketRepository(dataDir.resolve(FileTicketRepository.DEFAULT_FILE_NAME), store, warnings),
                new FileAttractionRepository(dataDir.resolve(FileAttractionRepository.DEFAULT_FILE_NAME), store,
                        warnings),
                new FilePassRepository(dataDir.resolve(FilePassRepository.DEFAULT_FILE_NAME), store, warnings),
                store);
    }

    /**
     * Wires the controllers with {@code repositories} and {@code hasher}, loads the stored data and
     * returns the park.
     */
    public static Park start(Repositories repositories, LocalDate today, PasswordHasher hasher) throws IOException {
        SystemController system = SystemController.getInstance();
        UserController users = UserController.getInstance();
        GroupController groups = GroupController.getInstance();
        TicketController tickets = TicketController.getInstance();
        AttractionController attractions = AttractionController.getInstance();
        PassController passes = PassController.getInstance();
        InvoiceController invoices = InvoiceController.getInstance();
        TransactionManager transactions = repositories.transactions();

        system.configure(today);
        users.configure(repositories.users(), transactions, hasher, tickets, groups, attractions);
        groups.configure(repositories.groups(), transactions, users, tickets);
        tickets.configure(repositories.tickets(), transactions, system, users, groups, passes);
        attractions.configure(repositories.attractions(), transactions, system, users, tickets, passes);
        passes.configure(repositories.passes(), transactions, system, tickets);
        invoices.configure(system, users, groups, tickets, passes);

        Park park = Park.getInstance();
        park.configure(system, users, groups, tickets, passes, attractions, invoices);
        park.load();
        return park;
    }
}
