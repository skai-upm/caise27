package es.upm.etsisi.park;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored.
 */
public class AttractionRepository extends TextFileRepository {

    private static final int FIELDS = 3;

    public AttractionRepository(Path file) {
        super(file);
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : readLines()) {
            String[] f = fields(line, FIELDS);
            int capacity = f.length == 0 ? -1 : parseCapacity(f[2]);
            if (capacity <= 0) {
                warnMalformed(line);
                continue;
            }
            attractions.add(new Attraction(f[0], f[1], capacity));
        }
        return attractions;
    }

    public void saveAll(Collection<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        writeLines(lines);
    }

    /** Returns the capacity, or -1 if the text is not an integer. */
    private static int parseCapacity(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
