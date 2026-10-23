package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writes the data files of the file repositories. Inside a transaction the
 * new file contents are only staged, and they are written together when the
 * transaction ends, so a failure leaves every file untouched.
 */
public class FileDataStore implements TransactionManager {

    private Map<Path, List<String>> staged;

    /** Replaces the content of {@code file}, now or at the end of the current transaction. */
    void write(Path file, List<String> lines) throws IOException {
        if (staged != null) {
            staged.put(file, lines);
        } else {
            TextFiles.writeAll(Map.of(file, lines));
        }
    }

    @Override
    public void inTransaction(IoAction action) throws IOException {
        if (staged != null) {
            action.run();
            return;
        }
        staged = new LinkedHashMap<>();
        try {
            action.run();
            TextFiles.writeAll(staged);
        } finally {
            staged = null;
        }
    }
}
