package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.model.Attraction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions, one per line: {@code id;name;capacity}. Occupants are not stored.
 */
public class AttractionRepository extends TextFileRepository {

    public AttractionRepository(Path file) {
        super(file);
    }

    public void save(Collection<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(join(attraction.getIdentifier(), attraction.getName(),
                    String.valueOf(attraction.getCapacity())));
        }
        writeLines(lines);
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            try {
                if (f.length != 3 || f[0].isEmpty() || f[1].isEmpty()) {
                    throw new InvalidValueException("expected id;name;capacity");
                }
                int capacity = Integer.parseInt(f[2]);
                if (capacity <= 0) {
                    throw new InvalidValueException("capacity must be positive");
                }
                attractions.add(new Attraction(f[0], f[1], capacity));
            } catch (InvalidValueException | NumberFormatException e) {
                problem(i + 1, line, e.getMessage());
            }
        }
        return attractions;
    }
}
