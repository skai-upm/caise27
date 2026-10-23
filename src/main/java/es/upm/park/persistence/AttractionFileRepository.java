package es.upm.park.persistence;

import es.upm.park.model.Attraction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never
 * stored, so every execution starts with empty attractions.
 */
public class AttractionFileRepository {

    private static final int FIELD_COUNT = 3;

    private final Path file;

    public AttractionFileRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> loadAll() {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] fields = TextFiles.split(line);
            try {
                if (fields.length != FIELD_COUNT || fields[0].isEmpty() || fields[1].isEmpty()) {
                    throw new IllegalArgumentException();
                }
                attractions.add(new Attraction(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (IllegalArgumentException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return attractions;
    }

    public void saveAll(Iterable<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR,
                    attraction.getId(), attraction.getName(), Integer.toString(attraction.getCapacity())));
        }
        TextFiles.writeLines(file, lines);
    }
}
