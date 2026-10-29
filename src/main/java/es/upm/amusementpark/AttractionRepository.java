package es.upm.amusementpark;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never persisted.
 */
public class AttractionRepository {

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public Map<String, Attraction> load() {
        Map<String, Attraction> attractions = new LinkedHashMap<>();
        for (String[] f : TextFiles.readRecords(file)) {
            if (f.length != 3) {
                TextFiles.warnMalformed(file);
                continue;
            }
            try {
                attractions.put(f[0], new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                TextFiles.warnMalformed(file);
            }
        }
        return attractions;
    }

    public void save(Iterable<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        TextFiles.writeLines(file, lines);
    }
}
