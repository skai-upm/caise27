package es.upm.etsisi.park.persistence.text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Line-based text file helpers shared by the repositories. */
final class TextFiles {

    static final String SEPARATOR = ";";

    private TextFiles() {
    }

    /** All lines of the file, or an empty list if it does not exist. */
    static List<String> readLines(Path file) throws IOException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        return Files.readAllLines(file, StandardCharsets.UTF_8);
    }

    /** Replaces the file contents, writing through a temporary sibling file. */
    static void write(Path file, List<String> lines) throws IOException {
        Path absolute = file.toAbsolutePath();
        Path parent = absolute.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Path temp = absolute.resolveSibling(absolute.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, absolute, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Splits a stored line into its fields, keeping empty trailing fields. */
    static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }
}
