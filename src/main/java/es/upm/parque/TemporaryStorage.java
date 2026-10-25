package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A per-run persistence directory {@code <base>/yyyyMMdd_HHmmss} used by --fly mode and deleted at the end.
 */
public final class TemporaryStorage {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path base;
    private final Path directory;

    private TemporaryStorage(Path base, Path directory) {
        this.base = base;
        this.directory = directory;
    }

    /**
     * Creates a new, empty timestamped directory under {@code base}.
     */
    public static TemporaryStorage create(Path base) {
        String stamp = LocalDateTime.now(ZoneId.systemDefault()).format(STAMP);
        try {
            Files.createDirectories(base);
            Path candidate = base.resolve(stamp);
            for (int suffix = 1; Files.exists(candidate); suffix++) {
                candidate = base.resolve(stamp + "_" + suffix);
            }
            return new TemporaryStorage(base, Files.createDirectory(candidate));
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create temporary directory under " + base, e);
        }
    }

    public Path getDirectory() {
        return directory;
    }

    /**
     * Deletes the run directory with all its files, and the base directory if it is left empty.
     * Safe to call more than once.
     */
    public synchronized void delete() {
        if (Files.exists(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Cannot delete temporary directory " + directory, e);
            }
        }
        try (Stream<Path> remaining = Files.list(base)) {
            if (remaining.findAny().isEmpty()) {
                Files.deleteIfExists(base);
            }
        } catch (IOException e) {
            // The base directory is shared by concurrent runs; leaving it in place is harmless.
        }
    }
}
