package es.upm.amusementpark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Timestamped data directory used by --fly mode ({@code tmp/yyyyMMdd_HHmmss}), deleted at the end of execution.
 */
public final class TemporaryDataDirectory {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path path;

    private TemporaryDataDirectory(Path path) {
        this.path = path;
    }

    public static TemporaryDataDirectory create(Path baseDirectory) {
        Path parent = baseDirectory.resolve("tmp");
        String name = LocalDateTime.now().format(TIMESTAMP);
        Path candidate = parent.resolve(name);
        for (int i = 1; Files.exists(candidate); i++) {
            candidate = parent.resolve(name + "_" + i);
        }
        try {
            Files.createDirectories(candidate);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create temporary directory " + candidate, e);
        }
        return new TemporaryDataDirectory(candidate);
    }

    public Path getPath() {
        return path;
    }

    /**
     * Deletes the directory with all its files, and the {@code tmp} parent if nothing else is left in it.
     */
    public void delete() {
        try {
            List<Path> entries;
            try (Stream<Path> walk = Files.walk(path)) {
                entries = walk.sorted(Comparator.reverseOrder()).toList();
            }
            for (Path entry : entries) {
                Files.deleteIfExists(entry);
            }
            Path parent = path.getParent();
            try (Stream<Path> rest = Files.list(parent)) {
                if (rest.findAny().isEmpty()) {
                    Files.deleteIfExists(parent);
                }
            }
        } catch (IOException e) {
            System.err.println("WARNING: cannot delete temporary directory " + path + ": " + e.getMessage());
        }
    }
}
