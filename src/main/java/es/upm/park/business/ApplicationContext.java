package es.upm.park.business;

import es.upm.park.persistence.AttractionFileStore;
import es.upm.park.persistence.DataDirectory;
import es.upm.park.persistence.GroupFileStore;
import es.upm.park.persistence.GroupTicketFileStore;
import es.upm.park.persistence.PassFileStore;
import es.upm.park.persistence.TicketFileStore;
import es.upm.park.persistence.UserFileStore;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Startup configuration of the application: creates the repositories for the chosen data
 * directory, injects them into the singleton controllers and wires the collaborations among
 * controllers and services. With a temporary (--fly) directory, {@link #close()} deletes it.
 */
public final class ApplicationContext implements AutoCloseable {

    private final ParkController controller;
    private final Path dataDirectory;
    private final Path temporaryDirectory;
    private final AtomicBoolean closed = new AtomicBoolean();

    private ApplicationContext(ParkController controller, Path dataDirectory, Path temporaryDirectory) {
        this.controller = controller;
        this.dataDirectory = dataDirectory;
        this.temporaryDirectory = temporaryDirectory;
    }

    /** Configures the application over the data files in {@code dataDir}, starting at the OS date. */
    public static ApplicationContext start(Path dataDir) throws IOException {
        return start(dataDir, LocalDate.now());
    }

    public static ApplicationContext start(Path dataDir, LocalDate initialDate) throws IOException {
        return new ApplicationContext(configure(dataDir, initialDate), dataDir, null);
    }

    /** Configures the application over a new, empty run directory under {@code tmpRoot}. */
    public static ApplicationContext startTemporary(Path tmpRoot) throws IOException {
        Path runDir = DataDirectory.createRunDirectory(tmpRoot);
        try {
            return new ApplicationContext(configure(runDir, LocalDate.now()), runDir, runDir);
        } catch (IOException | RuntimeException e) {
            DataDirectory.deleteQuietly(runDir);
            throw e;
        }
    }

    /** Creates the file repositories for {@code dataDir} and injects them and all collaborators. */
    private static ParkController configure(Path dataDir, LocalDate date) throws IOException {
        UserManager users = UserManager.configure(
                new UserFileStore(dataDir.resolve(DataDirectory.USERS_FILE)), new BCryptPasswordHasher());
        GroupManager groups = GroupManager.configure(
                new GroupFileStore(dataDir.resolve(DataDirectory.GROUPS_FILE)), users);
        TicketManager tickets = TicketManager.configure(
                new TicketFileStore(dataDir.resolve(DataDirectory.TICKETS_FILE)),
                new GroupTicketFileStore(dataDir.resolve(DataDirectory.GROUP_TICKETS_FILE)), users, groups);
        AttractionManager attractions = AttractionManager.configure(
                new AttractionFileStore(dataDir.resolve(DataDirectory.ATTRACTIONS_FILE)));
        PassManager passes = PassManager.configure(
                new PassFileStore(dataDir.resolve(DataDirectory.PASSES_FILE)), tickets);
        return ParkController.configure(users, groups, tickets, attractions, passes, new InvoiceDirector(), date);
    }

    public ParkController getController() {
        return controller;
    }

    /** Directory whose files the repositories of this context use. */
    public Path getDataDirectory() {
        return dataDirectory;
    }

    /** The temporary data directory, if this context was started with {@link #startTemporary}. */
    public Optional<Path> getTemporaryDirectory() {
        return Optional.ofNullable(temporaryDirectory);
    }

    /** Deletes the temporary data directory, if any. Safe to call more than once. */
    @Override
    public void close() {
        if (temporaryDirectory != null && closed.compareAndSet(false, true)) {
            DataDirectory.deleteQuietly(temporaryDirectory);
        }
    }
}
