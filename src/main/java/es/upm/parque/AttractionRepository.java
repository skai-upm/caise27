package es.upm.parque;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity} lines. The users inside are not stored.
 */
public class AttractionRepository {

    private static final String SEPARATOR = ";";

    private final TextFileStore store;

    public AttractionRepository(Path file) {
        this.store = new TextFileStore(file);
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : store.readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            if (f.length != 3) {
                store.warnSkipped(line, "expected 3 fields");
                continue;
            }
            try {
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                store.warnSkipped(line, "invalid capacity");
            }
        }
        return attractions;
    }

    public void saveAll(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity())));
        }
        store.writeLines(lines);
    }
}
