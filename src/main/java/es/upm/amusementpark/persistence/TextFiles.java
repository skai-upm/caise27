package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-based text file helpers shared by the storage classes.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    static List<String> readLines(Path file) throws IOException {
        return Files.exists(file) ? Files.readAllLines(file, StandardCharsets.UTF_8) : new ArrayList<>();
    }

    static void writeLines(Path file, List<String> lines) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }
}
