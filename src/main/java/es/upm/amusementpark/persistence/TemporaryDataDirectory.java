package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Timestamped data directory used by the --fly execution mode. Closing it
 * deletes the directory and everything written into it.
 */
public final class TemporaryDataDirectory implements AutoCloseable {

    private static final DateTimeFormatter NAME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final int MAX_ATTEMPTS = 1000;

    private final Path root;
    private final Path directory;
    private final boolean createdRoot;

    private TemporaryDataDirectory(Path root, Path directory, boolean createdRoot) {
        this.root = root;
        this.directory = directory;
        this.createdRoot = createdRoot;
    }

    /**
     * Creates {@code root/yyyyMMdd_HHmmss}, adding {@code _1}, {@code _2}, ...
     * when a directory with that name already exists.
     */
    public static TemporaryDataDirectory create(Path root, LocalDateTime now) throws IOException {
        boolean createdRoot = !Files.isDirectory(root);
        Files.createDirectories(root);
        String name = now.format(NAME_FORMAT);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Path candidate = root.resolve(attempt == 0 ? name : name + "_" + attempt);
            try {
                return new TemporaryDataDirectory(root, Files.createDirectory(candidate), createdRoot);
            } catch (FileAlreadyExistsException e) {
                // name taken by another run: try the next suffix
            }
        }
        throw new IOException("Cannot create a temporary directory under " + root);
    }

    public Path getDirectory() {
        return directory;
    }

    /** Deletes the directory with all its files, and the root if this run created it and it is empty. */
    @Override
    public void close() throws IOException {
        if (Files.exists(directory)) {
            List<Path> paths;
            try (Stream<Path> walk = Files.walk(directory)) {
                paths = walk.sorted(Comparator.reverseOrder()).toList();
            }
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
        if (createdRoot && Files.isDirectory(root)) {
            try (Stream<Path> remaining = Files.list(root)) {
                if (remaining.findAny().isEmpty()) {
                    Files.delete(root);
                }
            }
        }
    }
}
