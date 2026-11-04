package es.upm.park.persistence;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Locations of the data files and management of temporary per-run data directories.
 */
public final class DataDirectory {

    public static final String USERS_FILE = "users.txt";
    public static final String GROUPS_FILE = "groups.txt";
    public static final String TICKETS_FILE = "tickets.txt";
    public static final String GROUP_TICKETS_FILE = "grouptickets.txt";
    public static final String ATTRACTIONS_FILE = "attractions.txt";

    private static final DateTimeFormatter RUN_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DataDirectory() {
    }

    /** Creates {@code tmpRoot/yyyyMMdd_HHmmss}, adding a numeric suffix if that name is taken. */
    public static Path createRunDirectory(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String base = LocalDateTime.now().format(RUN_NAME);
        for (int attempt = 0; ; attempt++) {
            Path candidate = tmpRoot.resolve(attempt == 0 ? base : base + "_" + attempt);
            try {
                return Files.createDirectory(candidate);
            } catch (FileAlreadyExistsException e) {
                // another run started in the same second: try the next suffix
            }
        }
    }

    /** Deletes a directory tree, deepest entries first; problems are reported but not thrown. */
    public static void deleteQuietly(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            System.err.println("WARNING: Could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }
}
