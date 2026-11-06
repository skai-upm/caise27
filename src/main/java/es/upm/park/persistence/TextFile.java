package es.upm.park.persistence;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Line-based UTF-8 text file helpers shared by the repositories. */
final class TextFile {

    static final String SEPARATOR = ";";

    private TextFile() {
    }

    /** Reads the non-blank lines of a file; a missing file has no lines. */
    static List<String> readLines(Path file) {
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8).stream().filter(line -> !line.isBlank()).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read " + file, e);
        }
    }

    /** Writes all lines through a temporary file that then replaces the target. */
    static void writeLines(Path file, List<String> lines) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Path temp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(temp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write " + file, e);
        }
    }

    /** Parses each line, reporting and skipping the ones that cannot be interpreted. */
    static <T> List<T> parseAll(Path file, Function<String[], T> parser, PrintStream warnings) {
        List<T> result = new ArrayList<>();
        for (String line : readLines(file)) {
            try {
                result.add(parser.apply(line.split(SEPARATOR, -1)));
            } catch (RuntimeException e) {
                warnings.println("WARNING: Skipping unreadable line in " + file.getFileName() + ": " + line);
            }
        }
        return result;
    }

    static void requireFields(String[] fields, int minimum) {
        if (fields.length < minimum) {
            throw new IllegalArgumentException("Expected at least " + minimum + " fields");
        }
    }
}
