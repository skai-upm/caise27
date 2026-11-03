package es.upm.etsisi.amusementpark;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Line-based text file access shared by the repositories.
 */
final class TextFile {

    private TextFile() {
    }

    /** Reads all lines; a missing file is read as empty. */
    static List<String> readLines(Path file) throws ParkException {
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ParkException("cannot read file " + file, e);
        }
    }

    /**
     * Replaces the file contents. The lines are written to a temporary sibling
     * file first, so a failed write leaves the old file intact.
     */
    static void writeLines(Path file, List<String> lines) throws ParkException {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ParkException("cannot write file " + file, e);
        }
    }
}
