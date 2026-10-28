package es.upm.amusementpark.persistence.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.Attraction;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored.
 */
public class AttractionFileStorage {

    private final Path file;

    public AttractionFileStorage(Path file) {
        this.file = file;
    }

    public List<Attraction> load(List<String> warnings) throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        int n = 0;
        for (String line : TextFiles.readLines(file)) {
            n++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            try {
                if (f.length != 3) {
                    throw new NumberFormatException();
                }
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                warnings.add("Skipping malformed attraction at line " + n + " of " + file);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        TextFiles.writeLines(file, lines);
    }
}
