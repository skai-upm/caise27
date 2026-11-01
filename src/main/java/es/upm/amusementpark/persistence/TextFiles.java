package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * File helpers shared by the repositories.
 */
final class TextFiles {

    private TextFiles() {
    }

    /**
     * Replaces the file with the given lines so that readers see either the old
     * or the new content, never a partially written file: the lines are written
     * to a sibling temporary file that is then moved over the target.
     */
    static void writeAtomically(Path file, List<String> lines) throws IOException {
        Path temporary = file.resolveSibling(file.getFileName() + ".tmp");
        try {
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            try {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
