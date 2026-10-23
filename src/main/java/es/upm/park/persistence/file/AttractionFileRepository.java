package es.upm.park.persistence.file;

import es.upm.park.business.model.attraction.Attraction;
import es.upm.park.business.model.attraction.AttractionType;
import es.upm.park.persistence.AttractionRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;type}. Lines without a type
 * (Delivery 2 files) are read as GENERAL. Occupants are never stored, so
 * every execution starts with empty attractions.
 */
public class AttractionFileRepository implements AttractionRepository {

    private final Path file;

    public AttractionFileRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> loadAll() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] fields = TextFiles.split(line);
            try {
                if ((fields.length != 3 && fields.length != 4) || fields[0].isEmpty() || fields[1].isEmpty()) {
                    throw new IllegalArgumentException();
                }
                AttractionType type = fields.length == 3 ? AttractionType.GENERAL : AttractionType.parse(fields[3]);
                if (type == null) {
                    throw new IllegalArgumentException();
                }
                attractions.add(type.create(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (IllegalArgumentException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return attractions;
    }

    @Override
    public void saveAll(Iterable<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, attraction.getId(), attraction.getName(),
                    Integer.toString(attraction.getCapacity()), attraction.getType().name()));
        }
        TextFiles.writeLines(file, lines);
    }
}
