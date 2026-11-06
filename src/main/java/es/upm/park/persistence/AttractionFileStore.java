package es.upm.park.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.park.model.Attraction;

/**
 * Stores attractions as {@code id;name;capacity} lines. Occupants are not
 * stored: attractions always start empty.
 */
public class AttractionFileStore {

    private static final int FIELDS = 3;

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFile.read(file)) {
            String[] f = TextFile.fields(line);
            if (f.length == FIELDS && !f[0].isEmpty() && isPositiveNumber(f[2])) {
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(TextFile.SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        TextFile.write(file, lines);
    }

    private static boolean isPositiveNumber(String text) {
        return !text.isEmpty() && text.length() < 10 && text.chars().allMatch(c -> c >= '0' && c <= '9')
                && Integer.parseInt(text) > 0;
    }
}
