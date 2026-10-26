package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A per-execution data directory for --fly mode, named after the start time and deleted when the run ends.
 */
public class TemporaryDataDirectory {
    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path path;

    private TemporaryDataDirectory(Path path) {
        this.path = path;
    }

    public static TemporaryDataDirectory create(Path root, LocalDateTime now) {
        String base = NAME_FORMAT.format(now);
        Path candidate = root.resolve(base);
        for (int n = 1; Files.exists(candidate); n++) {
            candidate = root.resolve(base + "_" + n);
        }
        try {
            Files.createDirectories(candidate);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create temporary data directory " + candidate, e);
        }
        return new TemporaryDataDirectory(candidate);
    }

    public Path getPath() {
        return path;
    }

    /** Deletes the directory and everything inside it; does nothing if it is already gone. */
    public void delete() {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> files = Files.walk(path)) {
            for (Path p : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot delete temporary data directory " + path, e);
        }
    }
}
