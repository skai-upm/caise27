package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.repository.AttractionRepository;
import es.upm.etsisi.amusementpark.business.exception.InvalidValueException;
import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.AttractionType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions, one per line: {@code id;name;capacity;type}. Occupants are not stored;
 * lines without a type are read as GENERAL attractions.
 */
public class TextAttractionRepository extends TextFileRepository implements AttractionRepository {

    public TextAttractionRepository(Path file) {
        super(file);
    }

    @Override
    public void save(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(join(attraction.getIdentifier(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        writeLines(lines);
    }

    @Override
    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        List<String> lines = readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            try {
                if (f.length < 3 || f.length > 4 || f[0].isEmpty() || f[1].isEmpty()) {
                    throw new InvalidValueException("expected id;name;capacity;type");
                }
                AttractionType type = f.length == 4 ? AttractionType.parse(f[3]) : AttractionType.GENERAL;
                int capacity = Integer.parseInt(f[2]);
                if (capacity <= 0) {
                    throw new InvalidValueException("capacity must be positive");
                }
                attractions.add(type.create(f[0], f[1], capacity));
            } catch (InvalidValueException | NumberFormatException e) {
                problem(i + 1, line, e.getMessage());
            }
        }
        return attractions;
    }
}
