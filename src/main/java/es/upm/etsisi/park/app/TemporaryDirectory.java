package es.upm.etsisi.park.app;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Timestamped data directory used by --fly mode: {@code <base>/tmp/yyyyMMdd_HHmmss/}.
 */
final class TemporaryDirectory {

    static final String TMP_DIR = "tmp";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path path;
    private final Path parent;
    private final boolean parentCreated;

    private TemporaryDirectory(Path path, Path parent, boolean parentCreated) {
        this.path = path;
        this.parent = parent;
        this.parentCreated = parentCreated;
    }

    static TemporaryDirectory create(Path base) throws IOException {
        Path parent = base.resolve(TMP_DIR);
        boolean parentCreated = !Files.exists(parent);
        Files.createDirectories(parent);
        String stamp = LocalDateTime.now().format(TIMESTAMP);
        Path candidate = parent.resolve(stamp);
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = parent.resolve(stamp + "_" + suffix++);
        }
        Files.createDirectory(candidate);
        return new TemporaryDirectory(candidate, parent, parentCreated);
    }

    Path path() {
        return path;
    }

    /**
     * Deletes the directory and its files, and the {@code tmp} parent if this run created it and it is empty.
     */
    void delete(PrintStream err) {
        try (Stream<Path> walk = Files.walk(path)) {
            List<Path> entries = walk.sorted(Comparator.reverseOrder()).toList();
            for (Path entry : entries) {
                Files.deleteIfExists(entry);
            }
            if (parentCreated) {
                try (Stream<Path> remaining = Files.list(parent)) {
                    if (remaining.findAny().isEmpty()) {
                        Files.deleteIfExists(parent);
                    }
                }
            }
        } catch (IOException e) {
            err.println("WARNING: Cannot delete temporary directory " + path + ": " + e.getMessage());
        }
    }
}
