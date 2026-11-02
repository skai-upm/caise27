package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Creates and removes the timestamped data directory used by {@code --fly} executions.
 */
public final class TemporaryDataDirectory {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TemporaryDataDirectory() {
    }

    /** Creates {@code <parent>/<yyyyMMdd_HHmmss>}, adding a numeric suffix if that name is taken. */
    public static Path create(Path parent, LocalDateTime start) throws IOException {
        Files.createDirectories(parent);
        String base = start.format(STAMP);
        Path candidate = parent.resolve(base);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = parent.resolve(base + "_" + suffix++);
        }
        return Files.createDirectory(candidate);
    }

    /** Deletes the directory and its files, then its parent if it is left empty. */
    public static void delete(Path directory) throws IOException {
        if (Files.exists(directory)) {
            List<Path> paths;
            try (Stream<Path> walk = Files.walk(directory)) {
                paths = walk.sorted(Comparator.reverseOrder()).toList();
            }
            for (Path path : paths) {
                Files.delete(path);
            }
        }
        Path parent = directory.getParent();
        if (parent != null) {
            try {
                Files.deleteIfExists(parent);
            } catch (DirectoryNotEmptyException e) {
                // Other runs still use the parent directory.
            }
        }
    }
}
