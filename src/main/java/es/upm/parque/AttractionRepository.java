package es.upm.parque;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attraction definitions in a text file, one per line as id;name;capacity.
 * The users inside an attraction are not stored.
 */
public class AttractionRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 3;

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] parts = line.split(SEPARATOR, -1);
            Integer capacity = parts.length == FIELDS ? AttractionManager.parseCapacity(parts[2]) : null;
            if (capacity != null && !parts[0].isBlank()) {
                attractions.add(new Attraction(parts[0], parts[1], capacity));
            }
        }
        return attractions;
    }

    public void save(Collection<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity())));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
