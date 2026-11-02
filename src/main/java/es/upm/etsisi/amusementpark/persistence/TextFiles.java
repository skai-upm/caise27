package es.upm.etsisi.amusementpark.persistence;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Line-oriented text file helpers shared by the repositories.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    static List<String> readLines(Path file) throws IOException {
        return Files.exists(file) ? Files.readAllLines(file, StandardCharsets.UTF_8) : List.of();
    }

    /** Writes the lines to a sibling temporary file and moves it over the target. */
    static void writeLines(Path file, List<String> lines) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path tmp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
        try {
            moveOver(tmp, absolute);
        } catch (IOException e) {
            Files.deleteIfExists(tmp);
            throw e;
        }
    }

    private static void moveOver(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }
}
