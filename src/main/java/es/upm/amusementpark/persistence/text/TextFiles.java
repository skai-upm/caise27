package es.upm.amusementpark.persistence.text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import es.upm.amusementpark.business.exception.PersistenceException;

/**
 * Line-based text file helpers shared by the text repositories.
 */
final class TextFiles {

    static final String SEPARATOR = ";";

    private static final Logger LOG = Logger.getLogger(TextFiles.class.getName());

    private TextFiles() {
    }

    /** Non-blank lines split on ';', or an empty list if the file does not exist. */
    static List<String[]> readRecords(Path file) {
        List<String[]> records = new ArrayList<>();
        if (!Files.exists(file)) {
            return records;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    records.add(line.split(SEPARATOR, -1));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("cannot read file " + file, e);
        }
        return records;
    }

    static void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("cannot write file " + file, e);
        }
    }

    static void warnMalformed(Path file) {
        LOG.warning(() -> "skipping malformed line in " + file);
    }

    static String join(String... parts) {
        return String.join(SEPARATOR, parts);
    }
}
