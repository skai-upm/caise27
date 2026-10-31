package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/** Creates and removes the per-run storage directory used by {@code --fly} executions. */
public final class TemporaryStorage {

    private static final DateTimeFormatter RUN_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TemporaryStorage() {
    }

    /** Creates {@code tmpRoot/<yyyyMMdd_HHmmss>}, adding a suffix if that run directory already exists. */
    public static Path create(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now(ZoneId.systemDefault()).format(RUN_STAMP);
        Path candidate = tmpRoot.resolve(stamp);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = tmpRoot.resolve(stamp + "_" + suffix++);
        }
        return Files.createDirectory(candidate);
    }

    /** Deletes the run directory and every file in it. */
    public static void delete(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }
}
