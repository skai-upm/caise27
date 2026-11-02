package es.upm.etsisi.park;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Timestamped data directory used by {@code --fly} executions; deleted with all its files on close.
 */
public final class TemporaryStorage implements AutoCloseable {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path root;
    private final Path directory;

    private TemporaryStorage(Path root, Path directory) {
        this.root = root;
        this.directory = directory;
    }

    /** Creates {@code <base>/tmp/<yyyyMMdd_HHmmss>/}, adding a numeric suffix if that name is taken. */
    public static TemporaryStorage create(Path base, LocalDateTime now) throws IOException {
        Path root = base.resolve("tmp");
        Files.createDirectories(root);
        String stamp = now.format(STAMP);
        Path candidate = root.resolve(stamp);
        for (int n = 1; Files.exists(candidate); n++) {
            candidate = root.resolve(stamp + "_" + n);
        }
        return new TemporaryStorage(root, Files.createDirectory(candidate));
    }

    public Path getDirectory() {
        return directory;
    }

    @Override
    public void close() throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> paths = Files.walk(directory)) {
                for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                    Files.delete(p);
                }
            }
        }
        try (Stream<Path> remaining = Files.list(root)) {
            if (remaining.findAny().isEmpty()) {
                Files.delete(root);
            }
        }
    }
}
