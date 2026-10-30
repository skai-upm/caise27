package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Attraction;
import es.upm.etsisi.amusementpark.business.model.AttractionType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;type}. Lines without a type are GENERAL.
 * Occupants are never stored.
 */
public class AttractionFileStore {

    private final LineFile file;

    public AttractionFileStore(Path file) {
        this.file = new LineFile(file);
    }

    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String[] f : file.readRecords()) {
            AttractionType type = typeOf(f);
            try {
                if (type == null) {
                    throw new IllegalArgumentException("Wrong fields or type");
                }
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                file.warnMalformed(f);
            }
        }
        return attractions;
    }

    private static AttractionType typeOf(String[] f) {
        if (f.length == 3) {
            return AttractionType.GENERAL;
        }
        return f.length == 4 ? AttractionType.fromName(f[3]) : null;
    }

    public void save(List<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(LineFile.join(a.getId(), a.getName(), a.getCapacity(), a.getType()));
        }
        file.writeLines(lines);
    }
}
