package es.upm.parque.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;

/**
 * Stores attraction definitions in a text file, one per line as id;name;capacity;type.
 * Lines without a type are read as GENERAL attractions.
 * The users inside an attraction are not stored.
 */
public class TextFileAttractionRepository implements AttractionRepository {

    private static final String SEPARATOR = ";";
    private static final int LEGACY_FIELDS = 3;
    private static final int FIELDS = 4;

    private final Path file;

    public TextFileAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split(SEPARATOR, -1);
            boolean wellFormed = parts.length == FIELDS || parts.length == LEGACY_FIELDS;
            Integer capacity = wellFormed ? Attraction.parseCapacity(parts[2]) : null;
            AttractionType type = parts.length == FIELDS ? AttractionType.parse(parts[3]) : AttractionType.GENERAL;
            if (capacity != null && type != null && !parts[0].isBlank()) {
                attractions.add(type.create(parts[0], parts[1], capacity));
            }
        }
        return attractions;
    }

    @Override
    public void save(Collection<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
