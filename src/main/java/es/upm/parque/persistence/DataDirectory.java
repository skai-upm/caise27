package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Locations of the persistence files: the permanent directory, or a temporary per-run directory
 * for {@code --fly} executions.
 */
public final class DataDirectory {

    /** Directory of the permanent data, relative to the working directory. */
    public static final Path PERMANENT = Path.of("data");

    private static final DateTimeFormatter RUN_NAME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DataDirectory() {
    }

    /**
     * Creates {@code base/tmp/<yyyyMMdd_HHmmss>} named after the clock's current time, adding a
     * numeric suffix if a directory with that name already exists. The time only names the run;
     * it is not used by any business rule.
     */
    public static Path createTemporary(Path base, Clock clock) {
        String name = LocalDateTime.now(clock).format(RUN_NAME);
        Path tmp = base.resolve("tmp");
        Path dir = tmp.resolve(name);
        for (int i = 1; Files.exists(dir); i++) {
            dir = tmp.resolve(name + "_" + i);
        }
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create temporary data directory " + dir, e);
        }
    }

    /**
     * Deletes a directory and everything inside it; does nothing if it does not exist.
     */
    public static void deleteRecursively(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(dir)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path p : paths) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot delete temporary data directory " + dir, e);
        }
    }
}
