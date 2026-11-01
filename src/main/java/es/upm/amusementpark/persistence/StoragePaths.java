package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Locations of the persistence files. Normal and --fly executions differ only
 * in the directory given here.
 */
public class StoragePaths {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path directory;

    public StoragePaths(Path directory) {
        this.directory = directory;
    }

    public Path getDirectory() {
        return directory;
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

    /**
     * Creates a new directory {@code <tmpRoot>/<yyyyMMdd_HHmmss>} (with a numeric suffix
     * if that name is taken) for a temporary execution.
     */
    public static StoragePaths createTemporary(Path tmpRoot) throws IOException {
        String stamp = LocalDateTime.now().format(STAMP);
        Path dir = tmpRoot.resolve(stamp);
        for (int i = 1; Files.exists(dir); i++) {
            dir = tmpRoot.resolve(stamp + "_" + i);
        }
        return new StoragePaths(Files.createDirectories(dir));
    }

    /**
     * Deletes the directory and everything in it, on a best-effort basis.
     */
    public void deleteDirectory() {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    // best effort: keep deleting the remaining files
                }
            });
        } catch (IOException e) {
            // directory vanished or unreadable; nothing more to do
        }
    }
}
