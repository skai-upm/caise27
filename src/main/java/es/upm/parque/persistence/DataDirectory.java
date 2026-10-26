package es.upm.parque.persistence;

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
 * The directory holding every persistence file of one execution. It is either the permanent data directory
 * or a temporary one (used by {@code --fly}) that is deleted with all its files when closed.
 */
public final class DataDirectory implements AutoCloseable {

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path path;
    private final boolean temporary;

    private DataDirectory(Path path, boolean temporary) {
        this.path = path;
        this.temporary = temporary;
    }

    public static DataDirectory permanent(Path path) {
        return new DataDirectory(path, false);
    }

    /** Creates a new directory under {@code base} named after {@code start}, unique for this execution. */
    public static DataDirectory temporary(Path base, LocalDateTime start) {
        String name = start.format(NAME_FORMAT);
        try {
            Files.createDirectories(base);
            Path dir = base.resolve(name);
            for (int suffix = 1; Files.exists(dir); suffix++) {
                dir = base.resolve(name + "_" + suffix);
            }
            return new DataDirectory(Files.createDirectory(dir), true);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create temporary data directory in " + base, e);
        }
    }

    public Path getPath() {
        return path;
    }

    public boolean isTemporary() {
        return temporary;
    }

    /** @return the location of the persistence file with that name */
    public Path resolve(String fileName) {
        return path.resolve(fileName);
    }

    /** Deletes the directory and its files if it is temporary; does nothing otherwise or if already deleted. */
    @Override
    public void close() {
        if (!temporary || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path p : paths) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot delete temporary data directory " + path, e);
        }
    }
}
