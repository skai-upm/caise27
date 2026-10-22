package es.upm.etsisi.park.persistence.file;

import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;type}. Lines without a type (Delivery 2
 * data) are read as GENERAL. Occupants are never stored, so every attraction starts empty.
 */
public class AttractionFileStore {

    private static final String SEPARATOR = ";";
    private static final int LEGACY_FIELD_COUNT = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] fields = line.split(SEPARATOR, -1);
            try {
                if (fields.length != FIELD_COUNT && fields.length != LEGACY_FIELD_COUNT) {
                    throw new IllegalArgumentException("wrong field count");
                }
                AttractionType type = fields.length == FIELD_COUNT
                        ? AttractionType.fromText(fields[3]) : AttractionType.GENERAL;
                attractions.add(type.create(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (IllegalArgumentException | ValidationException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return attractions;
    }

    public void save(List<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        TextFiles.writeLines(file, lines);
    }
}
