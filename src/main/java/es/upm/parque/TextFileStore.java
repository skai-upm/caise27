package es.upm.parque;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads and writes the lines of one text file. Writes replace the whole file atomically.
 */
public class TextFileStore {

    private final Path file;

    public TextFileStore(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    /**
     * Returns the non-blank lines of the file, or an empty list when the file does not exist.
     */
    public List<String> readLines() throws IOException {
        List<String> lines = new ArrayList<>();
        if (!Files.exists(file)) {
            return lines;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (!line.isBlank()) {
                lines.add(line);
            }
        }
        return lines;
    }

    public void writeLines(List<String> lines) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent, file.getFileName().toString(), ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Reports a line that cannot be restored; loading continues with the next line.
     */
    public void warnSkipped(String line, String reason) {
        System.err.println("WARNING Skipping line in " + file + " (" + reason + "): " + line);
    }
}
