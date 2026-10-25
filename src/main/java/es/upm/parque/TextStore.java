package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-based UTF-8 text file access shared by the repositories.
 */
public final class TextStore {

    public static final String SEPARATOR = ";";

    private TextStore() {
    }

    /**
     * Returns the non-blank lines of the file split by {@link #SEPARATOR}; a missing file has no lines.
     */
    public static List<String[]> readRecords(Path file) {
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
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        return records;
    }

    /**
     * Replaces the file contents with the given lines, creating parent directories if needed.
     */
    public static void writeLines(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }

    public static void warnMalformed(Path file, String[] record) {
        System.err.println("WARNING: skipping malformed record in " + file + ": " + String.join(SEPARATOR, record));
    }
}
