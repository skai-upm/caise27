package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * A per-execution directory for persistence files (--fly mode), named after the start timestamp
 * and deleted with all its contents when the execution ends.
 */
public class TemporaryDataDirectory {

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path path;

    private TemporaryDataDirectory(Path path) {
        this.path = path;
    }

    /**
     * Creates {@code base/yyyyMMdd_HHmmss}, adding a numeric suffix if that directory already exists.
     */
    public static TemporaryDataDirectory create(Path base, LocalDateTime now) throws IOException {
        Files.createDirectories(base);
        String name = now.format(NAME_FORMAT);
        Path candidate = base.resolve(name);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = base.resolve(name + "_" + suffix++);
        }
        return new TemporaryDataDirectory(Files.createDirectory(candidate));
    }

    public Path getPath() {
        return path;
    }

    /**
     * Deletes the directory and everything in it. Calling it again has no effect.
     */
    public synchronized void delete() {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> files = Files.walk(path)) {
            for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(file);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete temporary directory " + path, e);
        }
    }
}
