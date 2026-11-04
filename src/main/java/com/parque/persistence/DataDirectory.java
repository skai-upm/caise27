package com.parque.persistence;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Location management for the data files: creation and deletion of the per-execution temporary directory
 * {@code tmp/<yyyyMMdd_HHmmss>/} used by {@code --fly}.
 */
public final class DataDirectory {
    public static final String FLY_ROOT = "tmp";

    private static final DateTimeFormatter FLY_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private DataDirectory() {
    }

    /**
     * Creates a new, empty {@code base/tmp/yyyyMMdd_HHmmss} directory, adding {@code _1}, {@code _2}... if a
     * directory with that timestamp already exists.
     */
    public static Path createTemporary(Path base, LocalDateTime start) throws IOException {
        Path root = base.resolve(FLY_ROOT);
        Files.createDirectories(root);
        String stamp = start.format(FLY_STAMP);
        for (int i = 0;; i++) {
            Path candidate = root.resolve(i == 0 ? stamp : stamp + "_" + i);
            try {
                return Files.createDirectory(candidate);
            } catch (FileAlreadyExistsException taken) {
                // try the next suffix
            }
        }
    }

    /** Deletes the directory and its files, and the {@code tmp} parent if it is left empty. Idempotent. */
    public static void delete(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (Stream<Path> walk = Files.walk(dir)) {
                for (Path p : walk.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(p);
                }
            }
        }
        Path root = dir.toAbsolutePath().getParent();
        if (root != null && Files.isDirectory(root)) {
            try (Stream<Path> children = Files.list(root)) {
                if (children.findAny().isEmpty()) {
                    Files.deleteIfExists(root);
                }
            }
        }
    }
}
