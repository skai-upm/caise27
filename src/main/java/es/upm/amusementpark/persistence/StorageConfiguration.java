package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.repository.Repositories;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Configures where the file repositories store their data and creates them. Normal and temporary
 * (--fly) execution use the same repository implementations; only the data directory differs.
 */
public class StorageConfiguration {

    private static final DateTimeFormatter TEMP_DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final Path dataDir;
    private final boolean temporary;
    private final Path createdTmpRoot;
    private final PrintStream warnings;

    private StorageConfiguration(Path dataDir, boolean temporary, Path createdTmpRoot, PrintStream warnings) {
        this.dataDir = dataDir;
        this.temporary = temporary;
        this.createdTmpRoot = createdTmpRoot;
        this.warnings = warnings;
    }

    /**
     * Persistent storage in the given directory.
     */
    public static StorageConfiguration persistent(Path dataDir, PrintStream warnings) {
        return new StorageConfiguration(dataDir, false, null, warnings);
    }

    /**
     * Creates a fresh {@code tmp/<yyyyMMdd_HHmmss>} directory under {@code baseDir} and stores everything
     * there. Call {@link #cleanup()} when execution ends.
     */
    public static StorageConfiguration temporary(Path baseDir, LocalDateTime now, PrintStream warnings)
            throws IOException {
        Path tmpRoot = baseDir.resolve("tmp");
        boolean rootCreated = !Files.exists(tmpRoot);
        Files.createDirectories(tmpRoot);
        String timestamp = now.format(TEMP_DIR_FORMAT);
        String name = timestamp;
        for (int suffix = 1; ; suffix++) {
            Path dir = tmpRoot.resolve(name);
            try {
                Files.createDirectory(dir);
                return new StorageConfiguration(dir, true, rootCreated ? tmpRoot : null, warnings);
            } catch (FileAlreadyExistsException e) {
                name = timestamp + "_" + suffix;
            }
        }
    }

    /**
     * New repository instances configured for this storage's data directory.
     */
    public Repositories createRepositories() {
        return new Repositories(
                new FileUserRepository(dataDir.resolve("users.txt"), warnings),
                new FileGroupRepository(dataDir.resolve("groups.txt"), warnings),
                new FileTicketRepository(dataDir.resolve("tickets.txt"), warnings),
                new FileAttractionRepository(dataDir.resolve("attractions.txt"), warnings),
                new FilePassRepository(dataDir.resolve("passes.txt"), warnings));
    }

    public Path getDataDir() {
        return dataDir;
    }

    public boolean isTemporary() {
        return temporary;
    }

    /**
     * Deletes the temporary data directory, and the {@code tmp} folder if this run created it and it is
     * now empty. Does nothing for persistent storage.
     */
    public void cleanup() throws IOException {
        if (!temporary) {
            return;
        }
        deleteRecursively(dataDir);
        if (createdTmpRoot != null) {
            try (Stream<Path> remaining = Files.list(createdTmpRoot)) {
                if (remaining.findAny().isEmpty()) {
                    Files.delete(createdTmpRoot);
                }
            }
        }
    }

    private static void deleteRecursively(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        List<Path> paths;
        try (Stream<Path> walk = Files.walk(dir)) {
            paths = walk.sorted(Comparator.reverseOrder()).toList();
        }
        for (Path path : paths) {
            Files.delete(path);
        }
    }
}
