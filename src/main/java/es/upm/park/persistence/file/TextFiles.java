package es.upm.park.persistence.file;

import es.upm.park.persistence.PersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-oriented text file access shared by the repositories.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    /**
     * Reads the non-blank lines of a file; a missing file has no lines.
     */
    static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            List<String> lines = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
            return lines;
        } catch (IOException e) {
            throw new PersistenceException("Cannot read data file " + file, e);
        }
    }

    /**
     * Replaces the file contents atomically where the file system allows it.
     */
    static void writeLines(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot write data file " + file, e);
        }
    }

    static String[] split(String line) {
        return line.split(SEPARATOR, -1);
    }

    static void warnMalformed(Path file, String line) {
        System.err.println("WARNING: skipping malformed line in " + file + ": " + line);
    }
}
