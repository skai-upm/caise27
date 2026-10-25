package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.model.Attraction;
import es.upm.etsisi.park.business.model.AttractionType;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;TYPE}. Lines without a type (Delivery 2) load as GENERAL.
 * Occupants are never persisted.
 */
public class AttractionFileRepository implements AttractionRepository {

    private static final int LEGACY_FIELD_COUNT = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;
    private final PrintStream warnings;

    public AttractionFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<Attraction<?>> loadAll() {
        List<Attraction<?>> attractions = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = TextFiles.fields(line);
            try {
                if (parts.length != FIELD_COUNT && parts.length != LEGACY_FIELD_COUNT) {
                    throw new IllegalArgumentException("unexpected number of fields");
                }
                AttractionType type = parts.length == FIELD_COUNT
                        ? AttractionType.fromName(parts[3]) : AttractionType.GENERAL;
                attractions.add(type.create(parts[0], parts[1], Integer.parseInt(parts[2])));
            } catch (RuntimeException e) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
            }
        }
        return attractions;
    }

    @Override
    public void saveAll(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, attraction.getId(), attraction.getName(),
                    Integer.toString(attraction.getCapacity()), attraction.getType().name()));
        }
        TextFiles.writeLines(file, lines);
    }
}
