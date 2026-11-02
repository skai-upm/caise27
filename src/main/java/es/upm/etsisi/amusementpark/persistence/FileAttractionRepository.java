package es.upm.etsisi.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import es.upm.etsisi.amusementpark.business.repository.AttractionRepository;

/**
 * Stores attractions one per line: identifier;name;capacity;type. Lines without a
 * type (Delivery 2 data) are read as GENERAL. Occupants are never stored.
 */
public class FileAttractionRepository implements AttractionRepository {
    private static final String SEPARATOR = ";";

    private final Path file;
    private final List<String> warnings = new ArrayList<>();

    public FileAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        warnings.clear();
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) {
                continue;
            }
            String[] f = lines.get(i).split(SEPARATOR, -1);
            AttractionType type = f.length == 4 ? AttractionType.fromName(f[3]) : AttractionType.GENERAL;
            try {
                if ((f.length != 3 && f.length != 4) || type == null) {
                    throw new NumberFormatException();
                }
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                warnings.add("skipped malformed attraction line " + (i + 1) + " in " + file);
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(String.join(SEPARATOR, a.getIdentifier(), a.getName(), String.valueOf(a.getCapacity()),
                    a.getType().name()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    @Override
    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }
}
