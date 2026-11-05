package es.upm.amusementpark.persistence.storage;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions in a text file, one per line: identifier;name;capacity;type. Occupants are not stored.
 */
public class AttractionFileStorage implements AttractionStorage {

    private final Path file;

    public AttractionFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            try {
                if (f.length == 3 || f.length == 4) {
                    attractions.add(type(f).create(f[0], f[1], Integer.parseInt(f[2])));
                }
            } catch (IllegalArgumentException e) {
                // malformed line: ignored
            }
        }
        return attractions;
    }

    /** Lines without a type (earlier versions) are general attractions. */
    private static AttractionType type(String[] f) {
        return f.length == 4 ? AttractionType.fromName(f[3]).orElseThrow(() -> new IllegalArgumentException(f[3]))
                : AttractionType.GENERAL;
    }

    @Override
    public void save(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, a.getIdentifier(), a.getName(), String.valueOf(a.getCapacity()),
                    a.getType().name()));
        }
        TextFiles.writeLines(file, lines);
    }
}
