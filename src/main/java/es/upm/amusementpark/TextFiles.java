package es.upm.amusementpark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-based text file helpers shared by the repositories.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    /** Non-blank lines split on ';', or an empty list if the file does not exist. */
    static List<String[]> readRecords(Path file) {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    records.add(line.split(SEPARATOR, -1));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read file " + file, e);
        }
        return records;
    }

    static void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write file " + file, e);
        }
    }

    static void warnMalformed(Path file) {
        System.err.println("WARNING: skipping malformed line in " + file);
    }
}
