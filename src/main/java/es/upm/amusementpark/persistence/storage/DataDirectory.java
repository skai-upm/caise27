package es.upm.amusementpark.persistence.storage;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Creates and deletes the timestamped data directory used by --fly mode.
 */
public final class DataDirectory {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DataDirectory() {
    }

    /** Creates {@code parent/<yyyyMMdd_HHmmss>} (suffixed with _1, _2... if it already exists). */
    public static Path create(Path parent) {
        try {
            Files.createDirectories(parent);
            String stamp = LocalDateTime.now().format(STAMP);
            Path dir = parent.resolve(stamp);
            for (int i = 1; Files.exists(dir); i++) {
                dir = parent.resolve(stamp + "_" + i);
            }
            return Files.createDirectory(dir);
        } catch (IOException e) {
            throw new PersistenceException("cannot manage temporary directory: " + e.getMessage(), e);
        }
    }

    /** Deletes the directory and everything inside it. */
    public static void delete(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        } catch (IOException e) {
            throw new PersistenceException("cannot manage temporary directory: " + e.getMessage(), e);
        }
    }
}
