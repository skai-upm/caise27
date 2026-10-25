package es.upm.parque;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as lines {@code id;name;capacity}. Occupancy is never stored.
 */
public class AttractionRepository {

    public static final String FILE_NAME = "attractions.txt";

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        for (String[] fields : TextStore.readRecords(file)) {
            try {
                if (fields.length != 3) {
                    throw new NumberFormatException();
                }
                attractions.add(new Attraction(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (NumberFormatException e) {
                TextStore.warnMalformed(file, fields);
            }
        }
        return attractions;
    }

    public void saveAll(Collection<Attraction> attractions) {
        TextStore.writeLines(file, attractions.stream()
                .map(attraction -> String.join(TextStore.SEPARATOR, attraction.getId(), attraction.getName(),
                        String.valueOf(attraction.getCapacity())))
                .toList());
    }
}
