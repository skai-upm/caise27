package com.amusementpark.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Per-run temporary storage directories used by --fly executions.
 */
public final class TemporaryStorage {

    private static final DateTimeFormatter DIR_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private TemporaryStorage() {
    }

    /** Creates {@code tmpRoot/<yyyyMMdd_HHmmss>}, adding a numeric suffix if that name is taken. */
    public static Path create(Path tmpRoot) throws IOException {
        Files.createDirectories(tmpRoot);
        String stamp = LocalDateTime.now().format(DIR_FORMAT);
        Path dir = tmpRoot.resolve(stamp);
        for (int n = 1; Files.exists(dir); n++) {
            dir = tmpRoot.resolve(stamp + "_" + n);
        }
        return Files.createDirectory(dir);
    }

    /** Deletes the directory and everything in it; reports but does not throw on failure. */
    public static void delete(Path dir) {
        if (!Files.exists(dir)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException | UncheckedIOException e) {
            System.err.println("WARNING: Could not delete temporary directory " + dir + ": " + e.getMessage());
        }
    }
}
