package es.upm.amusementpark;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and retrieves attractions. One attraction per line: {@code id;name;capacity}. Occupants are not
 * stored, so every attraction is empty after loading.
 */
public class AttractionRepository {

    public static final String DEFAULT_FILE = "attractions.txt";

    private final Path file;
    private final List<Attraction> attractions = new ArrayList<>();

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public void load() {
        attractions.clear();
        if (!Files.exists(file)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split(";", -1);
                try {
                    if (fields.length != 3) {
                        throw new IllegalArgumentException("wrong number of fields");
                    }
                    attractions.add(new Attraction(fields[0], fields[1], ParkFormat.parseCapacity(fields[2])));
                } catch (IllegalArgumentException e) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read attractions file " + file, e);
        }
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Attraction attraction : attractions) {
                writer.write(attraction.getId() + ";" + attraction.getName() + ";" + attraction.getCapacity());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write attractions file " + file, e);
        }
    }

    public List<Attraction> findAll() {
        return Collections.unmodifiableList(attractions);
    }

    public Attraction findById(String id) {
        for (Attraction attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    public void add(Attraction attraction) {
        attractions.add(attraction);
        save();
    }
}
