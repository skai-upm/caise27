package es.upm.etsisi.park.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for repositories that keep one record per line in a text file.
 * Only the file path differs between normal and temporary (--fly) runs.
 */
public abstract class TextFileRepository {

    protected static final String SEPARATOR = ";";

    private final Path file;

    protected TextFileRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /** Returns the non-blank lines of the file, or none if it does not exist yet. */
    protected List<String> readLines() throws IOException {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    /**
     * Replaces the file contents. Writes to a temporary file first so a failed
     * write never leaves a half-written data file.
     */
    protected void writeLines(List<String> lines) throws IOException {
        Path dir = file.toAbsolutePath().getParent();
        Files.createDirectories(dir);
        Path tmp = Files.createTempFile(dir, file.getFileName().toString(), ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            try {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    /** Splits a record; returns an empty array if it does not have the expected number of fields. */
    protected static String[] fields(String line, int expected) {
        String[] f = line.split(SEPARATOR, -1);
        return f.length == expected ? f : new String[0];
    }

    protected void warnMalformed(String line) {
        System.err.println("WARNING: skipping malformed record in " + file.getFileName() + ": " + line);
    }
}
