package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Creates and removes the timestamped data directory used by --fly mode.
 */
public final class TemporaryDirectory {
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TemporaryDirectory() {
    }

    /**
     * Creates {@code parent/<yyyyMMdd_HHmmss>} (with a numeric suffix if that name is taken).
     */
    public static Path create(Path parent) throws IOException {
        String stamp = LocalDateTime.now(ZoneId.systemDefault()).format(STAMP);
        Path dir = parent.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = parent.resolve(stamp + "_" + n);
        }
        return Files.createDirectories(dir);
    }

    /**
     * Deletes the directory with all its files, and its parent if that is left empty.
     */
    public static void delete(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(dir)) {
            paths = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path p : paths) {
            Files.delete(p);
        }
        Path parent = dir.toAbsolutePath().getParent();
        if (parent != null && isEmptyDirectory(parent)) {
            Files.delete(parent);
        }
    }

    private static boolean isEmptyDirectory(Path dir) throws IOException {
        try (Stream<Path> entries = Files.list(dir)) {
            return entries.findAny().isEmpty();
        }
    }
}
