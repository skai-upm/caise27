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
 * Locations of the park data: the permanent directory and the temporary,
 * timestamped directories used by the {@code --fly} mode.
 */
public final class DataDirectories {

    public static final String DATA_DIRECTORY = "data";
    public static final String TEMP_DIRECTORY = "tmp";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DataDirectories() {
    }

    public static Path permanent(Path baseDirectory) {
        return baseDirectory.resolve(DATA_DIRECTORY);
    }

    /** Creates a new {@code tmp/<yyyyMMdd_HHmmss>} directory (suffixed if it already exists). */
    public static Path createTemporary(Path baseDirectory) {
        try {
            Path parent = baseDirectory.resolve(TEMP_DIRECTORY);
            Files.createDirectories(parent);
            String name = LocalDateTime.now().format(TIMESTAMP);
            Path directory = parent.resolve(name);
            for (int suffix = 1; Files.exists(directory); suffix++) {
                directory = parent.resolve(name + "_" + suffix);
            }
            return Files.createDirectory(directory);
        } catch (IOException e) {
            throw new PersistenceException("could not create temporary data directory", e);
        }
    }

    /** Deletes a directory and everything inside it. */
    public static void delete(Path directory) {
        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> ordered = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path path : ordered) {
                Files.deleteIfExists(path);
            }
        } catch (IOException e) {
            throw new PersistenceException("could not delete temporary data: " + directory, e);
        }
    }
}
