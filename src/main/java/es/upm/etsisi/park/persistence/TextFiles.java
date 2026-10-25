package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-oriented text file helpers shared by the file repositories.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    /**
     * Reads all lines, returning an empty list when the file does not exist.
     */
    static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Cannot read file " + file, e);
        }
    }

    /**
     * Replaces the file contents atomically (write to a sibling temp file, then move).
     */
    static void writeLines(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenceException("Cannot write file " + file, e);
        }
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }
}
