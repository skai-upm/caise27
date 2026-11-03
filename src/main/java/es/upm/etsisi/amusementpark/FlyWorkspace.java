package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Temporary data directory for --fly executions, e.g. tmp/20260826_190315/.
 * Closing it deletes the directory and everything inside it.
 */
public final class FlyWorkspace implements AutoCloseable {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path root;
    private final Path directory;

    private FlyWorkspace(Path root, Path directory) {
        this.root = root;
        this.directory = directory;
    }

    /** Creates a new timestamped directory under {@code root}. */
    public static FlyWorkspace create(Path root, LocalDateTime now) throws ParkException {
        String name = now.format(TIMESTAMP);
        Path candidate = root.resolve(name);
        for (int suffix = 1; Files.exists(candidate); suffix++) {
            candidate = root.resolve(name + "_" + suffix);
        }
        try {
            Files.createDirectories(candidate);
        } catch (IOException e) {
            throw new ParkException("cannot create temporary directory " + candidate, e);
        }
        return new FlyWorkspace(root, candidate);
    }

    public Path getDirectory() {
        return directory;
    }

    /** Deletes the temporary directory, and its parent when nothing else is left in it. */
    @Override
    public void close() throws ParkException {
        try (Stream<Path> walk = Files.walk(directory)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path path : paths) {
                Files.delete(path);
            }
            try (Stream<Path> remaining = Files.list(root)) {
                if (remaining.findAny().isEmpty()) {
                    Files.delete(root);
                }
            }
        } catch (IOException e) {
            throw new ParkException("cannot delete temporary directory " + directory, e);
        }
    }
}
