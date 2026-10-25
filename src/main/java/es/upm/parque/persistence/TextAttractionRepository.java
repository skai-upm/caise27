package es.upm.parque.persistence;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.repository.AttractionRepository;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as lines {@code id;name;capacity;type}. Lines without a type are GENERAL.
 * Occupancy is never stored.
 */
public class TextAttractionRepository implements AttractionRepository {

    public static final String FILE_NAME = "attractions.txt";

    private final Path file;

    public TextAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String[] fields : TextStore.readRecords(file)) {
            AttractionType type = fields.length == 4 ? AttractionType.fromName(fields[3]) : AttractionType.GENERAL;
            try {
                if (fields.length < 3 || fields.length > 4 || type == null) {
                    throw new NumberFormatException();
                }
                attractions.add(type.create(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (NumberFormatException e) {
                TextStore.warnMalformed(file, fields);
            }
        }
        return attractions;
    }

    @Override
    public void saveAll(Collection<Attraction<?>> attractions) {
        TextStore.writeLines(file, attractions.stream()
                .map(attraction -> String.join(TextStore.SEPARATOR, attraction.getId(), attraction.getName(),
                        String.valueOf(attraction.getCapacity()), attraction.getType().name()))
                .toList());
    }
}
