package es.upm.amusementpark.persistence.text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.persistence.AttractionRepository;

/**
 * Stores attractions as {@code id;name;capacity;type}. Lines without a type (Delivery 2) are read as
 * GENERAL. Occupants are never persisted.
 */
public class TextAttractionRepository implements AttractionRepository {

    private final Path file;

    public TextAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public Map<String, Attraction<?>> load() {
        Map<String, Attraction<?>> attractions = new LinkedHashMap<>();
        for (String[] f : TextFiles.readRecords(file)) {
            AttractionType type = typeOf(f);
            if (type == null) {
                TextFiles.warnMalformed(file);
                continue;
            }
            try {
                attractions.put(f[0], type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                TextFiles.warnMalformed(file);
            }
        }
        return attractions;
    }

    private static AttractionType typeOf(String[] fields) {
        if (fields.length == 3) {
            return AttractionType.GENERAL;
        }
        if (fields.length == 4) {
            return AttractionType.fromName(fields[3]);
        }
        return null;
    }

    @Override
    public void save(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(TextFiles.join(a.getId(), a.getName(), String.valueOf(a.getCapacity()), a.getType().name()));
        }
        TextFiles.writeLines(file, lines);
    }
}
