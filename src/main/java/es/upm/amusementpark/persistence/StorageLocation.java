package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Directory holding every persistence file. Normal runs use the working directory; --fly runs use a
 * per-run temporary directory that is deleted at the end.
 */
public class StorageLocation {

    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path baseDir;
    private final boolean temporary;

    public StorageLocation(Path baseDir) {
        this(baseDir, false);
    }

    private StorageLocation(Path baseDir, boolean temporary) {
        this.baseDir = baseDir;
        this.temporary = temporary;
    }

    /** Creates {@code tmpRoot/yyyyMMdd_HHmmss} (with a numeric suffix if it already exists). */
    public static StorageLocation createTemporary(Path tmpRoot) throws IOException {
        String stamp = LocalDateTime.now().format(TIMESTAMP);
        Files.createDirectories(tmpRoot);
        Path dir = tmpRoot.resolve(stamp);
        int suffix = 1;
        while (Files.exists(dir)) {
            dir = tmpRoot.resolve(stamp + "_" + suffix++);
        }
        Files.createDirectory(dir);
        return new StorageLocation(dir, true);
    }

    public Path getBaseDir() {
        return baseDir;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public Path usersFile() {
        return baseDir.resolve("users.txt");
    }

    public Path groupsFile() {
        return baseDir.resolve("groups.txt");
    }

    public Path ticketsFile() {
        return baseDir.resolve("tickets.txt");
    }

    public Path groupTicketsFile() {
        return baseDir.resolve("grouptickets.txt");
    }

    public Path attractionsFile() {
        return baseDir.resolve("attractions.txt");
    }

    /** Deletes the directory and its contents if this is a temporary location. Safe to call twice. */
    public void deleteIfTemporary() {
        if (!temporary || !Files.exists(baseDir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(baseDir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException | UncheckedIOException e) {
            System.err.println("WARNING: could not delete temporary directory " + baseDir);
        }
    }
}
