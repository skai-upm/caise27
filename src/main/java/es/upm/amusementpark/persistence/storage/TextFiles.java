package es.upm.amusementpark.persistence.storage;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Small helpers to read and write UTF-8 text files line by line.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("cannot access data file '" + file + "'", e);
        }
    }

    static void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("cannot access data file '" + file + "'", e);
        }
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }
}
