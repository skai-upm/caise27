package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.model.Attraction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored, so every
 * attraction starts empty.
 */
public class AttractionFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 3;

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] fields = line.split(SEPARATOR, -1);
            try {
                if (fields.length != FIELD_COUNT) {
                    throw new IllegalArgumentException();
                }
                attractions.add(new Attraction(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (IllegalArgumentException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity())));
        }
        TextFiles.writeLines(file, lines);
    }
}
