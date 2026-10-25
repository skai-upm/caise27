package es.upm.parque;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Line-oriented access to a semicolon-separated data file, shared by the repositories.
 */
final class TextFile {

    private static final Logger LOGGER = Logger.getLogger(TextFile.class.getName());

    static final String SEPARATOR = ";";

    private TextFile() {
    }

    /**
     * Reads the non-blank lines of the file split into fields. Lines without the expected
     * number of fields are skipped with a warning. A missing file yields no records.
     */
    static List<String[]> readRecords(Path file, int fields) {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        try {
            int lineNumber = 0;
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                lineNumber++;
                String[] f = line.split(SEPARATOR, -1);
                if (f.length == fields) {
                    records.add(f);
                } else if (!line.isBlank()) {
                    int malformed = lineNumber;
                    LOGGER.warning(() -> "skipping malformed line " + malformed + " in " + file);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
        return records;
    }

    /**
     * Replaces the file content with one line per record, creating parent directories.
     */
    static void writeRecords(Path file, List<String> lines) {
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

    static String join(String... fields) {
        return String.join(SEPARATOR, fields);
    }
}
