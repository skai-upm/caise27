package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.Attraction;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never persisted.
 */
public class AttractionFileRepository {

    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public AttractionFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Attraction> loadAll() {
        List<Attraction> attractions = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = TextFiles.fields(line);
            try {
                if (parts.length != FIELD_COUNT) {
                    throw new IllegalArgumentException("expected " + FIELD_COUNT + " fields");
                }
                attractions.add(new Attraction(parts[0], parts[1], Integer.parseInt(parts[2])));
            } catch (IllegalArgumentException e) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
            }
        }
        return attractions;
    }

    public void saveAll(Collection<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, attraction.getId(), attraction.getName(),
                    Integer.toString(attraction.getCapacity())));
        }
        TextFiles.writeLines(file, lines);
    }
}
