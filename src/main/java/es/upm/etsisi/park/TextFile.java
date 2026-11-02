package es.upm.etsisi.park;

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
 * Reads and atomically writes line-based text files.
 */
final class TextFile {

    static final String SEPARATOR = ";";

    private TextFile() {
    }

    /** Returns the non-blank lines of the file, or no lines when the file does not exist. */
    static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            List<String> lines = new ArrayList<>();
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
            return lines;
        } catch (IOException e) {
            throw new ParkException("Cannot read " + file + ": " + e.getMessage(), e);
        }
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }

    static String join(String... fields) {
        return String.join(SEPARATOR, fields);
    }

    static void warnMalformed(Path file, String line) {
        System.err.println("WARNING: skipping malformed line in " + file + ": " + line);
    }

    static void write(Path file, List<String> lines) {
        writeAll(Map.of(file, lines));
    }

    /**
     * Writes every file to a temporary sibling first and only then moves them into place,
     * so a failure while writing leaves all previous files untouched.
     */
    static void writeAll(Map<Path, List<String>> contents) {
        Map<Path, Path> prepared = new LinkedHashMap<>();
        try {
            for (Map.Entry<Path, List<String>> e : contents.entrySet()) {
                Path parent = e.getKey().toAbsolutePath().getParent();
                Path tmp = Files.createTempFile(parent, "park", ".tmp");
                prepared.put(e.getKey(), tmp);
                Files.write(tmp, e.getValue(), StandardCharsets.UTF_8);
            }
            for (Map.Entry<Path, Path> e : prepared.entrySet()) {
                move(e.getValue(), e.getKey());
            }
        } catch (IOException e) {
            for (Path tmp : prepared.values()) {
                deleteQuietly(tmp);
            }
            throw new ParkException("Cannot write data files: " + e.getMessage(), e);
        }
    }

    private static void move(Path from, Path to) throws IOException {
        try {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best effort cleanup of a temporary file
        }
    }
}
