package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain text file accessed line by line. A missing file reads as empty.
 */
public class LineFile {

    static final String SEPARATOR = ";";

    private final Path path;

    public LineFile(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    /**
     * Returns the non-blank lines of the file split by ';', keeping empty fields.
     */
    public List<String[]> readRecords() {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(path)) {
            return records;
        }
        try {
            for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    records.add(line.split(SEPARATOR, -1));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot read file " + path, e);
        }
        return records;
    }

    public void writeLines(List<String> lines) {
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Cannot write file " + path, e);
        }
    }

    public void warnMalformed(String[] fields) {
        System.err.println("WARNING: skipping malformed line in " + path + ": " + String.join(SEPARATOR, fields));
    }

    public static String join(Object... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(fields[i] == null ? "" : fields[i]);
        }
        return line.toString();
    }
}
