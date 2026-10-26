package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Line-based UTF-8 file access shared by the repositories.
 */
final class TextFiles {

    private TextFiles() {
    }

    /**
     * Reads every line of the file; a missing file has no lines.
     */
    static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read file " + file, e);
        }
    }

    /**
     * Replaces the file contents with the given lines, creating parent directories if needed.
     */
    static void writeLines(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write file " + file, e);
        }
    }
}
