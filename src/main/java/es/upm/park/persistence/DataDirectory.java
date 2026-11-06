package es.upm.park.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Directory holding the park's data files. A permanent directory is left as
 * it is; a temporary one ({@code --fly}) is created with a timestamp name and
 * deleted with all its files when closed.
 */
public final class DataDirectory implements AutoCloseable {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path path;
    private final boolean temporary;

    private DataDirectory(Path path, boolean temporary) {
        this.path = path;
        this.temporary = temporary;
    }

    public static DataDirectory permanent(Path path) {
        return new DataDirectory(path, false);
    }

    /**
     * Creates {@code base/yyyyMMdd_HHmmss} (with a numeric suffix if that
     * name is taken).
     */
    public static DataDirectory temporary(Path base, LocalDateTime now) {
        try {
            Files.createDirectories(base);
            String name = now.format(STAMP);
            Path dir = base.resolve(name);
            for (int i = 1; Files.exists(dir); i++) {
                dir = base.resolve(name + "_" + i);
            }
            return new DataDirectory(Files.createDirectory(dir), true);
        } catch (IOException e) {
            throw new PersistenceException("cannot create temporary directory in " + base, e);
        }
    }

    public Path path() {
        return path;
    }

    public boolean isTemporary() {
        return temporary;
    }

    /** Deletes a temporary directory and its files; does nothing otherwise. */
    @Override
    public void close() {
        if (!temporary || !Files.exists(path)) {
            return;
        }
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path p : paths) {
                Files.delete(p);
            }
        } catch (IOException e) {
            throw new PersistenceException("cannot delete temporary directory " + path, e);
        }
    }
}
