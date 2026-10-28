package es.upm.amusementpark.persistence.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Directory holding the data files of the park. Normal and temporary persistence differ only in
 * this location.
 */
public record DataLocation(Path directory) {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * Creates a new temporary location {@code root/<yyyyMMdd_HHmmss>} (with a numeric suffix if
     * that directory already exists).
     */
    public static DataLocation temporary(Path root) throws IOException {
        String stamp = LocalDateTime.now().format(STAMP);
        Path dir = root.resolve(stamp);
        for (int i = 1; Files.exists(dir); i++) {
            dir = root.resolve(stamp + "_" + i);
        }
        return new DataLocation(Files.createDirectories(dir));
    }

    /**
     * Deletes the directory and every file inside it.
     */
    public void delete() throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path p : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(p);
            }
        }
    }

    public Path users() {
        return directory.resolve("users.txt");
    }

    public Path groups() {
        return directory.resolve("groups.txt");
    }

    public Path tickets() {
        return directory.resolve("tickets.txt");
    }

    public Path attractions() {
        return directory.resolve("attractions.txt");
    }
}
