package es.upm.parque;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions in a text file, one per line: {@code id;name;capacity}. Visitors are not stored.
 */
public class AttractionRepository {

    private static final String SEPARATOR = ";";

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> loadAll() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(SEPARATOR, -1);
                if (fields.length == 3) {
                    try {
                        attractions.add(new Attraction(fields[0], fields[1], Integer.parseInt(fields[2])));
                    } catch (NumberFormatException e) {
                        // malformed line: skipped
                    }
                }
            }
        }
        return attractions;
    }

    public void saveAll(Collection<Attraction> attractions) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Attraction attraction : attractions) {
                writer.write(attraction.getId() + SEPARATOR + attraction.getName() + SEPARATOR + attraction.getCapacity());
                writer.newLine();
            }
        }
    }
}
