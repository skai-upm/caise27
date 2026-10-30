package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Temporary data directories used by the --fly execution mode.
 */
public final class DataDirectory {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DataDirectory() {
    }

    /**
     * Creates a new directory {@code <root>/<yyyyMMdd_HHmmss>} (with a numeric suffix if taken).
     */
    public static Path createTemporary(Path root) {
        String stamp = LocalDateTime.now().format(STAMP);
        Path dir = root.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = root.resolve(stamp + "_" + n);
        }
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new PersistenceException("Cannot create temporary directory " + dir, e);
        }
    }

    /**
     * Deletes the directory with all its files, and its parent if that leaves it empty.
     */
    public static void deleteTemporary(Path dir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
            Path parent = dir.getParent();
            try (Stream<Path> siblings = Files.list(parent)) {
                if (siblings.findAny().isEmpty()) {
                    Files.delete(parent);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot delete temporary directory " + dir, e);
        }
    }
}
