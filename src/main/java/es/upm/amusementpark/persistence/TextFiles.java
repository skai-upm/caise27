package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Small helpers shared by the repositories.
 */
final class TextFiles {

    private TextFiles() {
    }

    /**
     * Reads all lines of a file, or none if it does not exist yet.
     */
    static List<String> readLines(Path path) throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }
}
