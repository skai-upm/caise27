package es.upm.park.business.service;

import es.upm.park.persistence.AttractionFileStorage;
import es.upm.park.persistence.DataFiles;
import es.upm.park.persistence.GroupFileStorage;
import es.upm.park.persistence.GroupTicketFileStorage;
import es.upm.park.persistence.TemporaryDataDirectory;
import es.upm.park.persistence.TicketFileStorage;
import es.upm.park.persistence.UserFileStorage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Opens the park's data — permanent files in a working directory, or a temporary
 * per-run directory for {@code --fly} — and builds the {@link ParkController} on it.
 * Closing a temporary session deletes its directory.
 */
public final class ParkSession implements AutoCloseable {

    private final ParkController controller;
    private final Path temporaryDir;
    private final Thread cleanupOnShutdown;

    private ParkSession(ParkController controller, Path temporaryDir, Thread cleanupOnShutdown) {
        this.controller = controller;
        this.temporaryDir = temporaryDir;
        this.cleanupOnShutdown = cleanupOnShutdown;
    }

    /** Uses the permanent data files in {@code workDir}. */
    public static ParkSession permanent(Path workDir) {
        return new ParkSession(build(DataFiles.in(workDir)), null, null);
    }

    /**
     * Creates {@code tmpRoot/yyyyMMdd_HHmmss[_n]} for this run and uses it for all data;
     * the directory is deleted on {@link #close()} or on JVM shutdown.
     */
    public static ParkSession temporary(Path tmpRoot, LocalDateTime start) {
        Path dir;
        try {
            dir = TemporaryDataDirectory.create(tmpRoot, start);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot create temporary directory: " + e.getMessage(), e);
        }
        Thread hook = new Thread(() -> deleteQuietly(dir));
        Runtime.getRuntime().addShutdownHook(hook);
        try {
            return new ParkSession(build(DataFiles.in(dir)), dir, hook);
        } catch (RuntimeException e) {
            removeHook(hook);
            deleteQuietly(dir);
            throw e;
        }
    }

    /**
     * Composition root: creates the repositories for {@code files} and initializes the
     * singleton controllers in dependency order, injecting repositories and collaborators.
     */
    private static ParkController build(DataFiles files) {
        try {
            UserManager users = UserManager.initialize(new UserFileStorage(files.users()));
            GroupManager groups = GroupManager.initialize(new GroupFileStorage(files.groups()), users);
            TicketManager tickets = TicketManager.initialize(new TicketFileStorage(files.tickets()),
                    new GroupTicketFileStorage(files.groupTickets()), users, groups);
            AttractionManager attractions =
                    AttractionManager.initialize(new AttractionFileStorage(files.attractions()));
            return ParkController.initialize(users, groups, tickets, attractions, new InvoiceDirector());
        } catch (RuntimeException e) {
            resetControllers();
            throw e;
        }
    }

    /** Ends the execution of every singleton controller. */
    public static void resetControllers() {
        ParkController.reset();
        AttractionManager.reset();
        TicketManager.reset();
        GroupManager.reset();
        UserManager.reset();
    }

    public ParkController controller() {
        return controller;
    }

    /** Directory of a temporary session, or {@code null} for permanent data. */
    public Path temporaryDirectory() {
        return temporaryDir;
    }

    @Override
    public void close() {
        resetControllers();
        if (temporaryDir != null) {
            deleteQuietly(temporaryDir);
            removeHook(cleanupOnShutdown);
        }
    }

    private static void deleteQuietly(Path dir) {
        try {
            TemporaryDataDirectory.deleteRecursively(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot delete temporary directory " + dir, e);
        }
    }

    private static void removeHook(Thread hook) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException e) {
            // JVM already shutting down: the hook itself performs the cleanup.
        }
    }
}
