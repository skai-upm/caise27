package es.upm.amusementpark;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Stores the persistent representation of one kind of park element as the
 * lines of a single text file. The file location is the only configuration.
 */
public abstract class TextFileRepository {

    static final String SEPARATOR = ";";

    private final Path file;

    protected TextFileRepository(Path file) {
        this.file = Objects.requireNonNull(file);
    }

    public Path getFile() {
        return file;
    }

    /** @return the stored lines, or an empty list when the file does not exist yet */
    protected List<String> readLines() {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("could not read " + file, e);
        }
    }

    protected void writeLines(List<String> lines) {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("could not write " + file, e);
        }
    }

    protected static String join(Object... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(fields[i]);
        }
        return line.toString();
    }

    protected static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }
}
