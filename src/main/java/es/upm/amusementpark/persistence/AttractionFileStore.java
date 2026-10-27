package es.upm.amusementpark.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.amusementpark.model.Attraction;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored.
 */
public class AttractionFileStore {

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFiles.read(file)) {
            String[] f = TextFiles.fields(line);
            if (f.length == 3) {
                try {
                    attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
                } catch (NumberFormatException e) {
                    // malformed line: skipped
                }
            }
        }
        return attractions;
    }

    public void save(Collection<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(TextFiles.join(a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        TextFiles.write(file, lines);
    }
}
