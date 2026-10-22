package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.exception.PersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-based text file access shared by all stores.
 */
final class TextFiles {

    private TextFiles() {
    }

    /** Non-blank lines of the file; an empty list when it does not exist. */
    static List<String> readLines(Path file) {
        List<String> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    result.add(line);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot read data file " + file, e);
        }
        return result;
    }

    /** Replaces the file contents atomically (temporary file + move). */
    static void writeLines(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            Path temp = Files.createTempFile(parent, "park", ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PersistenceException("Cannot write data file " + file, e);
        }
    }

    static void warnMalformed(Path file, String line) {
        System.err.println("WARNING: skipping malformed line in " + file + ": " + line);
    }
}
