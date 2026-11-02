package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.attraction.Attraction;

/**
 * Stores attractions one per line: identifier;name;capacity. Occupants are never stored.
 */
public class AttractionStorage {
    private static final String SEPARATOR = ";";

    private final Path file;
    private final List<String> warnings = new ArrayList<>();

    public AttractionStorage(Path file) {
        this.file = file;
    }

    public List<Attraction> load() throws IOException {
        warnings.clear();
        List<Attraction> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            String[] f = lines.get(i).split(SEPARATOR, -1);
            try {
                if (f.length != 3) {
                    throw new NumberFormatException();
                }
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                warnings.add("skipped malformed attraction line " + (i + 1) + " in " + file);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(SEPARATOR, a.getIdentifier(), a.getName(), String.valueOf(a.getCapacity())));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
