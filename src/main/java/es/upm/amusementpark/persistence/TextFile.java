package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

/**
 * A UTF-8 text file of records, one per line, with fields separated by {@code ;}.
 */
public class TextFile {

    /** Field separator; command fields can never contain it. */
    public static final String SEPARATOR = ";";

    private final Path path;

    public TextFile(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public Path getPath() {
        return path;
    }

    /** @return the lines of the file, or an empty list if it does not exist yet */
    public List<String> readLines() throws IOException {
        if (!Files.exists(path)) {
            return List.of();
        }
        return Files.readAllLines(path, StandardCharsets.UTF_8);
    }

    /** Replaces the file content atomically (write to a temporary file, then move). */
    public void writeLines(List<String> lines) throws IOException {
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temporary = parent.resolve(path.getFileName() + ".tmp");
        Files.write(temporary, lines, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** Splits a record into its fields, keeping empty trailing fields. */
    public static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }

    /** Joins fields into a record. */
    public static String join(String... fields) {
        return String.join(SEPARATOR, fields);
    }
}
