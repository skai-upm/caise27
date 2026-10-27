package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-based reading and writing of UTF-8 text files shared by the file stores.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    static List<String> read(Path file) {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Could not read " + file, e);
        }
    }

    static void write(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Could not write " + file, e);
        }
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }

    static String join(String... values) {
        return String.join(SEPARATOR, values);
    }
}
