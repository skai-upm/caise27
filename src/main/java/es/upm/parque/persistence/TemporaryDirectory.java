package es.upm.parque.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Per-execution persistence directory used by --fly mode.
 */
public final class TemporaryDirectory {

    private static final DateTimeFormatter NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TemporaryDirectory() {
    }

    /**
     * Creates base/yyyyMMdd_HHmmss for the given instant, adding _1, _2... if that name is taken.
     */
    public static Path create(Path base, LocalDateTime now) throws IOException {
        String name = NAME.format(now);
        Path dir = base.resolve(name);
        for (int suffix = 1; Files.exists(dir); suffix++) {
            dir = base.resolve(name + "_" + suffix);
        }
        return Files.createDirectories(dir);
    }

    /**
     * Deletes the directory and everything inside it. Does nothing if it no longer exists.
     */
    public static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        List<Path> entries;
        try (Stream<Path> walk = Files.walk(dir)) {
            entries = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path entry : entries) {
            Files.delete(entry);
        }
    }
}
