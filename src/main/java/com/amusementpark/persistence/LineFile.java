package com.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared data access for the semicolon-separated text files used by every storage class.
 */
public class LineFile {

    static final String SEPARATOR = ";";

    private final Path file;

    public LineFile(Path file) {
        this.file = file;
    }

    /**
     * Reads every non-blank line split into fields. A missing file yields no records;
     * lines whose number of fields is not one of {@code fieldCounts} are skipped with a warning.
     */
    public List<String[]> read(int... fieldCounts) throws IOException {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = line.split(SEPARATOR, -1);
            if (!allowed(fields.length, fieldCounts)) {
                System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                continue;
            }
            records.add(fields);
        }
        return records;
    }

    private static boolean allowed(int count, int[] fieldCounts) {
        for (int c : fieldCounts) {
            if (c == count) {
                return true;
            }
        }
        return false;
    }

    /** Replaces the file content with the given lines. */
    public void write(List<String> lines) throws IOException {
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public static String join(String... fields) {
        return String.join(SEPARATOR, fields);
    }
}
