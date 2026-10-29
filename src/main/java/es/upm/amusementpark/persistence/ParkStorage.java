package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Configures where every repository stores its file. Normal and temporary (--fly) execution use the
 * same repositories; only the data directory differs.
 */
public class ParkStorage {

    private static final DateTimeFormatter TEMP_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path dataDir;
    private final boolean temporary;
    private final Path createdTmpRoot;
    private final UserRepository users;
    private final GroupRepository groups;
    private final TicketRepository tickets;
    private final AttractionRepository attractions;

    private ParkStorage(Path dataDir, boolean temporary, Path createdTmpRoot, PrintStream warnings) {
        this.dataDir = dataDir;
        this.temporary = temporary;
        this.createdTmpRoot = createdTmpRoot;
        this.users = new UserRepository(dataDir.resolve("users.txt"), warnings);
        this.groups = new GroupRepository(dataDir.resolve("groups.txt"), warnings);
        this.tickets = new TicketRepository(dataDir.resolve("tickets.txt"), warnings);
        this.attractions = new AttractionRepository(dataDir.resolve("attractions.txt"), warnings);
    }

    /**
     * Persistent storage in the given directory.
     */
    public static ParkStorage persistent(Path dataDir, PrintStream warnings) {
        return new ParkStorage(dataDir, false, null, warnings);
    }

    /**
     * Creates a fresh {@code tmp/<yyyyMMdd_HHmmss>} directory under {@code baseDir} and stores everything there.
     * Call {@link #cleanup()} when execution ends.
     */
    public static ParkStorage temporary(Path baseDir, LocalDateTime now, PrintStream warnings) throws IOException {
        Path tmpRoot = baseDir.resolve("tmp");
        boolean rootCreated = !Files.exists(tmpRoot);
        Files.createDirectories(tmpRoot);
        String timestamp = now.format(TEMP_DIR_FORMAT);
        String name = timestamp;
        for (int suffix = 1; ; suffix++) {
            Path dir = tmpRoot.resolve(name);
            try {
                Files.createDirectory(dir);
                return new ParkStorage(dir, true, rootCreated ? tmpRoot : null, warnings);
            } catch (FileAlreadyExistsException e) {
                name = timestamp + "_" + suffix;
            }
        }
    }

    public Path getDataDir() {
        return dataDir;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public UserRepository users() {
        return users;
    }

    public GroupRepository groups() {
        return groups;
    }

    public TicketRepository tickets() {
        return tickets;
    }

    public AttractionRepository attractions() {
        return attractions;
    }

    /**
     * Deletes the temporary data directory, and the {@code tmp} folder if this run created it and it is
     * now empty. Does nothing for persistent storage.
     */
    public void cleanup() throws IOException {
        if (!temporary) {
            return;
        }
        deleteRecursively(dataDir);
        if (createdTmpRoot != null) {
            try (Stream<Path> remaining = Files.list(createdTmpRoot)) {
                if (remaining.findAny().isEmpty()) {
                    Files.delete(createdTmpRoot);
                }
            }
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(dir)) {
            paths = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path path : paths) {
            Files.delete(path);
        }
    }
}
