package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.StorageException;
import es.upm.etsisi.amusementpark.business.Values;
import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.repository.AttractionRepository;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores attractions in a text file, one per line: Identifier;Name;Capacity;Type.
 * Lines without a type (Delivery 2 data) are GENERAL attractions. Occupants are
 * not stored, so every attraction loads empty.
 */
public class TextAttractionRepository implements AttractionRepository {

    private static final String SEPARATOR = ";";
    private static final int LEGACY_FIELD_COUNT = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;
    private final PrintStream warnings;

    public TextAttractionRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<Attraction> load() throws StorageException {
        List<Attraction> attractions = new ArrayList<>();
        Set<String> identifiers = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFile.readLines(file)) {
            lineNumber++;
            if (!line.isBlank()) {
                Attraction attraction = parse(line.split(SEPARATOR, -1));
                if (attraction != null && identifiers.add(attraction.getIdentifier())) {
                    attractions.add(attraction);
                } else {
                    warnings.println("WARNING: skipping invalid attraction record at line " + lineNumber + " of " + file);
                }
            }
        }
        return attractions;
    }

    private static Attraction parse(String[] fields) {
        if (fields.length != LEGACY_FIELD_COUNT && fields.length != FIELD_COUNT) {
            return null;
        }
        int capacity = Values.parseCapacity(fields[2]);
        AttractionType type = fields.length == FIELD_COUNT ? AttractionType.fromName(fields[3]) : AttractionType.GENERAL;
        if (capacity <= 0 || type == null || fields[0].isBlank()) {
            return null;
        }
        return type.create(fields[0], fields[1], capacity);
    }

    @Override
    public void save(List<Attraction> attractions) throws StorageException {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getIdentifier(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        TextFile.writeLines(file, lines);
    }
}
