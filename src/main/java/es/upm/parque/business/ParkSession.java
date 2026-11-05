package es.upm.parque.business;

import java.io.UncheckedIOException;
import java.nio.file.Path;

import es.upm.parque.persistence.ParkFileStore;
import es.upm.parque.persistence.StorageLocation;
import es.upm.parque.persistence.UserFileStore;
import es.upm.parque.persistence.UserRepository;

/**
 * One execution of the park backed by its data files: chooses the storage location from the program
 * arguments (normal or {@code --fly}), loads the park, and releases temporary storage when closed.
 */
public final class ParkSession implements AutoCloseable {

    public static final String USERS_FILE = "users.txt";

    private final StorageLocation location;
    private final int ignoredUserLines;
    private final int ignoredParkLines;

    private ParkSession(StorageLocation location, int ignoredUserLines, int ignoredParkLines) {
        this.location = location;
        this.ignoredUserLines = ignoredUserLines;
        this.ignoredParkLines = ignoredParkLines;
    }

    /**
     * Opens the park storage selected by the arguments, relative to {@code workDir}, and starts the execution.
     *
     * @throws ParkException when the stored data cannot be read; temporary storage is released first
     */
    public static ParkSession open(String[] args, Path workDir) throws ParkException {
        StorageLocation location = StorageLocation.fromArgs(args, workDir);
        Path dataDir = location.getDirectory();
        ParkFileStore store = new ParkFileStore(dataDir);
        try {
            UserRepository users = new UserRepository(new UserFileStore(dataDir.resolve(USERS_FILE)));
            ParkController.getInstance().startExecution(users, store);
            return new ParkSession(location, users.getIgnoredOnLoad(), store.getMalformedLineCount());
        } catch (UncheckedIOException e) {
            location.cleanUp();
            throw new InvalidDataException("cannot read the park data files in '" + dataDir.toAbsolutePath() + "' ("
                    + USERS_FILE + ", " + ParkFileStore.GROUPS_FILE + ", " + ParkFileStore.TICKETS_FILE + ", "
                    + ParkFileStore.ATTRACTIONS_FILE + ")");
        }
    }

    /** Program arguments left once the storage option has been consumed (the optional command file). */
    public String[] getCommandArgs() {
        return location.getCommandArgs();
    }

    public int getIgnoredUserLines() {
        return ignoredUserLines;
    }

    public int getIgnoredParkLines() {
        return ignoredParkLines;
    }

    /** Releases the storage: deletes the temporary directory in {@code --fly} mode. */
    @Override
    public void close() {
        location.cleanUp();
    }
}
