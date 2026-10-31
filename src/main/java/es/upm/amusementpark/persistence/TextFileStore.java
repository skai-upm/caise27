package es.upm.amusementpark.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        return readRecords(file, fieldCount, fieldCount);
    }

    /**
     * Like {@link #readRecords(Path, int)} but accepting lines with between
     * {@code minFields} and {@code maxFields} fields, so files written by an
     * earlier delivery without newer optional columns still load.
     */
    public static List<String[]> readRecords(Path file, int minFields, int maxFields) throws IOException {
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
                if (fields.length < minFields || fields.length > maxFields) {
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
        writeAll(Map.of(file, records));
    }

    /**
     * Replaces several files as one save. Every file is first written to a
     * {@code .tmp} sibling; only when all of them have been written are they
     * moved over the originals. If writing any of them fails, the temporary
     * files written so far are removed and every original file is left untouched.
     */
    public static void writeAll(Map<Path, List<String[]>> files) throws IOException {
        Map<Path, Path> staged = new LinkedHashMap<>();
        try {
            for (Map.Entry<Path, List<String[]>> entry : files.entrySet()) {
                Path target = entry.getKey();
                Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
                staged.put(target, temporary);
                write(temporary, entry.getValue());
            }
        } catch (IOException e) {
            for (Path temporary : staged.values()) {
                if (Files.isRegularFile(temporary)) {
                    Files.delete(temporary);
                }
            }
            throw e;
        }
        for (Map.Entry<Path, Path> entry : staged.entrySet()) {
            move(entry.getValue(), entry.getKey());
        }
    }

    private static void write(Path file, List<String[]> records) throws IOException {
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

    private static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
