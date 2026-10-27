package es.upm.amusementpark.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;

/**
 * Stores attractions as {@code id;name;capacity;type} (lines without type are GENERAL).
 * Occupants are never stored.
 */
public class AttractionFileStore {

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFiles.read(file)) {
            String[] f = TextFiles.fields(line);
            AttractionType type = f.length == 3 ? AttractionType.GENERAL
                    : f.length == 4 ? AttractionType.fromText(f[3]) : null;
            if (type != null) {
                try {
                    attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
                } catch (NumberFormatException e) {
                    // malformed line: skipped
                }
            }
        }
        return attractions;
    }

    public void save(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(TextFiles.join(a.getId(), a.getName(), String.valueOf(a.getCapacity()), a.getType().name()));
        }
        TextFiles.write(file, lines);
    }
}
