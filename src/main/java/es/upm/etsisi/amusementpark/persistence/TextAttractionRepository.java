package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.exception.ValidationException;
import es.upm.etsisi.amusementpark.business.model.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.model.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.repository.AttractionRepository;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;type}. Lines without a type (Delivery 2 format) are
 * read as GENERAL attractions. Occupants are never stored.
 */
public class TextAttractionRepository implements AttractionRepository {

    private final Path file;

    public TextAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            try {
                if (f.length != 3 && f.length != 4) {
                    throw new IllegalArgumentException();
                }
                AttractionType type = f.length == 4 ? AttractionType.parse(f[3]) : AttractionType.GENERAL;
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException | ValidationException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity()),
                    a.getType().name()));
        }
        TextFiles.writeLines(file, lines);
    }
}
