package es.upm.park.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes UTF-8 text files line by line.
 */
final class TextFile {

    static final String SEPARATOR = ";";

    private TextFile() {
    }

    /** Lines of the file; a missing file has no lines. */
    static List<String> read(Path file) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("cannot read " + file, e);
        }
    }

    static void write(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("cannot write " + file, e);
        }
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }
}
