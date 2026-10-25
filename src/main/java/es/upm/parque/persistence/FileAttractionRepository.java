package es.upm.parque.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;

/**
 * Stores attractions as {@code id;name;capacity;type} lines; lines without a type are GENERAL attractions.
 */
public class FileAttractionRepository implements AttractionRepository {

    private static final String SEPARATOR = ";";

    private final TextFileStore store;

    public FileAttractionRepository(Path file) {
        this.store = new TextFileStore(file);
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : store.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 3 && f.length != 4) {
                store.warnSkipped(line, "expected 3 or 4 fields");
                continue;
            }
            AttractionType type = f.length == 4 ? AttractionType.fromName(f[3]) : AttractionType.GENERAL;
            if (type == null) {
                store.warnSkipped(line, "unknown type " + f[3]);
                continue;
            }
            try {
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                store.warnSkipped(line, "invalid capacity");
            }
        }
        return attractions;
    }

    @Override
    public void saveAll(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        store.writeLines(lines);
    }
}
