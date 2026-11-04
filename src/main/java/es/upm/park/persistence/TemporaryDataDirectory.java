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
 * Per-execution directory used by the {@code --fly} mode: {@code <root>/yyyyMMdd_HHmmss}.
 */
public final class TemporaryDataDirectory {

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TemporaryDataDirectory() {
    }

    /** Creates a new directory named after {@code start}, adding {@code _n} if the name is taken. */
    public static Path create(Path root, LocalDateTime start) throws IOException {
        String base = start.format(NAME_FORMAT);
        Path dir = root.resolve(base);
        for (int n = 1; Files.exists(dir); n++) {
            dir = root.resolve(base + "_" + n);
        }
        return Files.createDirectories(dir);
    }

    /** Deletes {@code dir} and everything in it; does nothing if it does not exist. */
    public static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(dir)) {
            paths = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path p : paths) {
            Files.deleteIfExists(p);
        }
    }
}
