package com.amusementpark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Users present are not stored.
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
            String[] fields = line.split(";", -1);
            if (fields.length != 3) {
                continue;
            }
            int capacity = Validator.parseCapacity(fields[2]);
            if (capacity > 0) {
                attractions.add(new Attraction(fields[0], fields[1], capacity));
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(attraction.getId() + ";" + attraction.getName() + ";" + attraction.getCapacity());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
