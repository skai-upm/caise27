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
 * Location of the persistence files. Normal runs use a permanent directory;
 * {@code --fly} runs use a per-run temporary directory. Repositories only
 * differ in the paths they receive from here.
 */
public record StorageConfig(Path directory) {

    private static final DateTimeFormatter RUN_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

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

    /** Creates a new directory {@code <baseDir>/tmp/<yyyyMMdd_HHmmss>} for a fly run. */
    public static StorageConfig createTemporary(Path baseDir, LocalDateTime startedAt) throws IOException {
        Path tmp = baseDir.resolve("tmp");
        Files.createDirectories(tmp);
        String stamp = startedAt.format(RUN_STAMP);
        Path run = tmp.resolve(stamp);
        for (int suffix = 1; Files.exists(run); suffix++) {
            run = tmp.resolve(stamp + "_" + suffix);
        }
        return new StorageConfig(Files.createDirectory(run));
    }

    /** Deletes this directory and its files, and its parent if that is left empty. */
    public void deleteDirectory() throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(directory)) {
            paths = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path path : paths) {
            Files.delete(path);
        }
        Path parent = directory.toAbsolutePath().getParent();
        try (Stream<Path> remaining = Files.list(parent)) {
            if (remaining.findAny().isEmpty()) {
                Files.delete(parent);
            }
        }
    }
}
