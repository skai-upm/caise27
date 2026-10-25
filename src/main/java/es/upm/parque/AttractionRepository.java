package es.upm.parque;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attraction definitions in a text file: id;name;capacity. Occupancy is never stored.
 */
public class AttractionRepository {

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
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(";", -1);
            try {
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (RuntimeException e) {
                System.err.println("WARNING skipping malformed attraction line: " + line);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        Files.createDirectories(file.toAbsolutePath().getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Attraction a : attractions) {
                writer.write(a.getId() + ";" + a.getName() + ";" + a.getCapacity());
                writer.newLine();
            }
        }
    }
}
