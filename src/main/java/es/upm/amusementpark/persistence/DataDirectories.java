package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.exception.PersistenceException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Creation and removal of the temporary data directories used by the
 * {@code --fly} execution mode.
 */
public final class DataDirectories {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DataDirectories() {
    }

    /** Creates a new directory inside {@code baseDir} named after the current date and time. */
    public static Path createTemporary(Path baseDir) {
        String stamp = LocalDateTime.now().format(STAMP);
        Path dir = baseDir.resolve(stamp);
        for (int i = 1; Files.exists(dir); i++) {
            dir = baseDir.resolve(stamp + "_" + i);
        }
        try {
            return Files.createDirectories(dir);
        } catch (IOException e) {
            throw new PersistenceException("Could not create temporary directory " + dir, e);
        }
    }

    /** Deletes the directory and its files, and {@code baseDir} if it is left empty. */
    public static void deleteTemporary(Path dir, Path baseDir) {
        try (Stream<Path> paths = Files.walk(dir)) {
            List<Path> all = paths.sorted(Comparator.reverseOrder()).toList();
            for (Path path : all) {
                Files.deleteIfExists(path);
            }
            try (Stream<Path> rest = Files.list(baseDir)) {
                if (rest.findAny().isEmpty()) {
                    Files.deleteIfExists(baseDir);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not delete temporary directory " + dir, e);
        }
    }
}
