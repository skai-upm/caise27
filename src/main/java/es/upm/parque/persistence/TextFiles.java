package es.upm.parque.persistence;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Replaces the contents of several files as one operation: every target is checked and its new
     * contents written to a temporary sibling first; only if all of that succeeds are the temporary
     * files moved over the targets. If preparation fails, no target is modified.
     */
    static void writeAll(Map<Path, List<String>> contents) {
        Map<Path, Path> prepared = new LinkedHashMap<>();
        try {
            for (Map.Entry<Path, List<String>> entry : contents.entrySet()) {
                Path target = entry.getKey();
                if (Files.exists(target) && !Files.isWritable(target)) {
                    throw new IOException("File is not writable: " + target);
                }
                Path parent = target.toAbsolutePath().getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
                Files.write(temporary, entry.getValue(), StandardCharsets.UTF_8);
                prepared.put(target, temporary);
            }
            for (Map.Entry<Path, Path> entry : prepared.entrySet()) {
                Files.move(entry.getValue(), entry.getKey(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            prepared.values().forEach(TextFiles::deleteQuietly);
            throw new UncheckedIOException("Cannot write files " + contents.keySet(), e);
        }
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // best effort: a leftover temporary file does not affect the stored data
        }
    }
}
