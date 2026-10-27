package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.exception.PersistenceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reading and writing of the ';'-separated text files used for persistence.
 */
final class TextFiles {

    private TextFiles() {
    }

    /** The non-blank lines of the file split by ';', or none if it does not exist. */
    static List<String[]> readRecords(Path file) {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    records.add(line.split(";", -1));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Could not read file " + file, e);
        }
        return records;
    }

    static void writeLines(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Could not write file " + file, e);
        }
    }
}
