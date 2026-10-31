package es.upm.amusementpark.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes text files made of {@code ;}-separated records, one per
 * line. Shared by every repository so that data access is implemented once.
 */
public final class TextFileStore {

    static final String SEPARATOR = ";";

    private TextFileStore() {
    }

    /**
     * Returns the records of the file. A missing file means no records; blank
     * lines are ignored and lines with a different number of fields are
     * skipped with a warning.
     */
    public static List<String[]> readRecords(Path file, int fieldCount) throws IOException {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(SEPARATOR, -1);
                if (fields.length != fieldCount) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                    continue;
                }
                records.add(fields);
            }
        }
        return records;
    }

    /** Replaces the file content with the given records. */
    public static void writeRecords(Path file, List<String[]> records) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (String[] record : records) {
                writer.write(String.join(SEPARATOR, record));
                writer.newLine();
            }
        }
    }
}
