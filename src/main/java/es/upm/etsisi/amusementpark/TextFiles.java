package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-based text file access shared by the repositories.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    /**
     * Returns the non-blank lines of the file, or an empty list if it does not exist.
     */
    static List<String> readLines(Path file) {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read file " + file, e);
        }
        return lines;
    }

    /**
     * Replaces the file content, writing to a temporary file first.
     */
    static void writeLines(Path file, List<String> lines) {
        try {
            Path parent = file.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            Path tmp = Files.createTempFile(parent, file.getFileName().toString(), ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write file " + file, e);
        }
    }

    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }

    static void warnMalformed(Path file, String line) {
        System.err.println("WARNING: skipping malformed line in " + file.getFileName() + ": " + line);
    }
}
