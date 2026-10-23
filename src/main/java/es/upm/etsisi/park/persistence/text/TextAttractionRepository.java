package es.upm.etsisi.park.persistence.text;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.business.exception.ValidationException;
import es.upm.etsisi.park.business.model.attraction.Attraction;
import es.upm.etsisi.park.business.model.attraction.AttractionType;
import es.upm.etsisi.park.persistence.AttractionRepository;

/**
 * Stores attractions one per line: {@code id;name;capacity;type}. Lines
 * without type (Delivery 2 format) load as GENERAL. Occupants are not
 * stored, so every attraction is empty after loading.
 */
public class TextAttractionRepository implements AttractionRepository {

    public static final String FILE_NAME = "attractions.txt";

    private static final int LEGACY_FIELD_COUNT = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;
    private final PrintStream warnings;

    public TextAttractionRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        List<String> lines = TextFiles.readLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = TextFiles.fields(line);
            try {
                if (fields.length != FIELD_COUNT && fields.length != LEGACY_FIELD_COUNT) {
                    throw new IllegalArgumentException("unexpected field count");
                }
                AttractionType type = fields.length == FIELD_COUNT ? AttractionType.parse(fields[3]) : AttractionType.GENERAL;
                attractions.add(type.create(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (IllegalArgumentException | ValidationException e) {
                warnings.println("WARNING: skipping malformed line " + (i + 1) + " in " + file);
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        TextFiles.write(file, lines);
    }
}
