package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Attraction;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores attractions in a text file, one per line: {@code id;name;capacity}.
 * Occupants are never stored, so attractions always start empty.
 */
public class FileAttractionRepository {

    public static final String DEFAULT_FILE_NAME = "attractions.txt";

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public FileAttractionRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    Path getFile() {
        return file;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }
            Attraction attraction = parse(line);
            if (attraction == null || !ids.add(attraction.getId())) {
                warnings.println("WARNING: skipping line " + lineNumber + " of " + file
                        + ": malformed or duplicate attraction");
            } else {
                attractions.add(attraction);
            }
        }
        return attractions;
    }

    static List<String> toLines(Collection<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity())));
        }
        return lines;
    }

    private static Attraction parse(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != FIELD_COUNT || fields[0].trim().isEmpty() || fields[1].trim().isEmpty()) {
            return null;
        }
        try {
            int capacity = Integer.parseInt(fields[2].trim());
            return capacity > 0 ? new Attraction(fields[0].trim(), fields[1].trim(), capacity) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
