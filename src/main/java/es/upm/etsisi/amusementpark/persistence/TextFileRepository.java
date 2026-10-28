package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Common text-file handling for repositories: one record per line, fields
 * separated by {@code ;}. The file location is the only configuration.
 */
abstract class TextFileRepository {

    static final String SEPARATOR = ";";

    private final Path file;
    private final List<String> problems = new ArrayList<>();

    protected TextFileRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /** Problems found while loading (malformed lines are skipped). */
    public List<String> getProblems() {
        return List.copyOf(problems);
    }

    protected void problem(int lineNumber, String line, String reason) {
        problems.add(file.getFileName() + " line " + lineNumber + " ignored (" + reason + "): " + line);
    }

    /** Non-empty lines of the file, or none if it does not exist yet. */
    protected List<String> readLines() {
        problems.clear();
        if (!Files.exists(file)) {
            return List.of();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Cannot read " + file, e);
        }
    }

    protected void writeLines(List<String> lines) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("Cannot write " + file, e);
        }
    }

    protected static String[] fields(String line) {
        return line.split(SEPARATOR, -1);
    }

    protected static String join(String... values) {
        return String.join(SEPARATOR, values);
    }
}
