package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.repository.AttractionRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attraction definitions as {@code id;name;capacity;type}. Lines written
 * by E2 have no type and load as {@code GENERAL}. Users present are runtime
 * state and are never stored, so attractions always start empty.
 */
public class FileAttractionRepository implements AttractionRepository {

    private static final int E2_FIELDS = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;

    public FileAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> loadAll() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String[] fields : TextFileStore.readRecords(file, E2_FIELDS, FIELD_COUNT)) {
            AttractionType type = fields.length == FIELD_COUNT
                    ? AttractionType.fromName(fields[3]) : AttractionType.GENERAL;
            if (type == null) {
                System.err.println("WARNING: skipping attraction " + fields[0] + " with unknown type " + fields[3]);
                continue;
            }
            try {
                attractions.add(type.create(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (NumberFormatException e) {
                System.err.println("WARNING: skipping attraction " + fields[0] + " with invalid capacity");
            }
        }
        return attractions;
    }

    @Override
    public void saveAll(Collection<Attraction<?>> attractions) throws IOException {
        List<String[]> records = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            records.add(new String[] {attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()});
        }
        TextFileStore.writeRecords(file, records);
    }
}
