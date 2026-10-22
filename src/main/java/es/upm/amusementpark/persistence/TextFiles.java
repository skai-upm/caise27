package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reading and writing of the line-based data files.
 */
final class TextFiles {

    private TextFiles() {
    }

    /** Returns the lines of {@code file}, or no lines if it does not exist. */
    static List<String> readLines(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    /**
     * Replaces the content of every given file. All new contents are first
     * written to temporary files, and only when every write succeeded are they
     * moved into place, so a failed write leaves all files untouched.
     */
    static void writeAll(Map<Path, List<String>> contents) throws IOException {
        Map<Path, Path> temps = new LinkedHashMap<>();
        try {
            for (Map.Entry<Path, List<String>> entry : contents.entrySet()) {
                Path parent = entry.getKey().toAbsolutePath().getParent();
                Files.createDirectories(parent);
                Path temp = Files.createTempFile(parent, "park", ".tmp");
                temps.put(entry.getKey(), temp);
                Files.write(temp, entry.getValue(), StandardCharsets.UTF_8);
            }
            for (Map.Entry<Path, Path> entry : temps.entrySet()) {
                move(entry.getValue(), entry.getKey());
            }
        } finally {
            for (Path temp : temps.values()) {
                Files.deleteIfExists(temp);
            }
        }
    }

    private static void move(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
