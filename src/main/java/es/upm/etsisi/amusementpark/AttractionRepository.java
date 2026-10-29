package es.upm.etsisi.amusementpark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored.
 */
public class AttractionRepository {

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = TextFiles.fields(line);
            try {
                if (f.length != 3) {
                    throw new IllegalArgumentException();
                }
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                TextFiles.warnMalformed(file, line);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        TextFiles.writeLines(file, lines);
    }
}
