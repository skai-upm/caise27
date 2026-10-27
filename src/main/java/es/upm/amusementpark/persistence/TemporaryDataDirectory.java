package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Data directory of a --fly execution: {@code tmp/<yyyyMMdd_HHmmss>/}, deleted when the execution ends.
 */
public class TemporaryDataDirectory {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path parent;
    private final Path directory;

    private TemporaryDataDirectory(Path parent, Path directory) {
        this.parent = parent;
        this.directory = directory;
    }

    /** Creates a new timestamped directory under {@code <base>/tmp}. */
    public static TemporaryDataDirectory create(Path base) {
        Path parent = base.resolve("tmp");
        String stamp = LocalDateTime.now().format(STAMP);
        try {
            Files.createDirectories(parent);
            Path dir = parent.resolve(stamp);
            for (int n = 1; Files.exists(dir); n++) {
                dir = parent.resolve(stamp + "_" + n);
            }
            Files.createDirectory(dir);
            return new TemporaryDataDirectory(parent, dir);
        } catch (IOException e) {
            throw new PersistenceException("Could not create temporary directory in " + parent, e);
        }
    }

    public Path path() {
        return directory;
    }

    /** Deletes the directory and all its files, and the tmp parent when left empty. */
    public void delete() {
        try (Stream<Path> walk = Files.walk(directory)) {
            List<Path> paths = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path p : paths) {
                Files.deleteIfExists(p);
            }
            try (Stream<Path> rest = Files.list(parent)) {
                if (rest.findAny().isEmpty()) {
                    Files.deleteIfExists(parent);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not delete temporary directory " + directory, e);
        }
    }
}
