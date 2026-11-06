package es.upm.park.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;

/**
 * Stores attractions as {@code id;name;capacity;type} lines (Delivery 2
 * lines without type load as GENERAL). Occupants are not stored:
 * attractions always start empty.
 */
public class AttractionFileStore {

    private static final int LEGACY_FIELDS = 3;
    private static final int FIELDS = 4;

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFile.read(file)) {
            String[] f = TextFile.fields(line);
            boolean shape = (f.length == LEGACY_FIELDS || f.length == FIELDS) && !f[0].isEmpty();
            AttractionType type = f.length == FIELDS ? AttractionType.fromName(f[3]) : AttractionType.GENERAL;
            if (shape && type != null && isPositiveNumber(f[2])) {
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            }
        }
        return attractions;
    }

    public void save(List<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(String.join(TextFile.SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity()),
                    a.getType().name()));
        }
        TextFile.write(file, lines);
    }

    private static boolean isPositiveNumber(String text) {
        return !text.isEmpty() && text.length() < 10 && text.chars().allMatch(c -> c >= '0' && c <= '9')
                && Integer.parseInt(text) > 0;
    }
}
