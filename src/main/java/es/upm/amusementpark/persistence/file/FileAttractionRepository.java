package es.upm.amusementpark.persistence.file;

import es.upm.amusementpark.business.exception.InvalidDataException;
import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.model.ParkFormat;
import es.upm.amusementpark.persistence.AttractionRepository;
import es.upm.amusementpark.persistence.PersistenceException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores and retrieves attractions. One attraction per line: {@code id;name;capacity;type}; lines from Delivery 2
 * without a type are read as GENERAL. Occupants are not stored, so every attraction is empty after loading.
 */
public class FileAttractionRepository implements AttractionRepository {

    public static final String FILE_NAME = "attractions.txt";

    private final Path file;
    private final List<Attraction<?>> attractions = new ArrayList<>();

    public FileAttractionRepository(Path file) {
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
                    if (fields.length != 3 && fields.length != 4) {
                        throw new InvalidDataException("wrong number of fields");
                    }
                    AttractionType type = fields.length == 4 ? AttractionType.fromName(fields[3]) : AttractionType.GENERAL;
                    attractions.add(type.create(fields[0], fields[1], ParkFormat.parseCapacity(fields[2])));
                } catch (InvalidDataException e) {
                    System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot read attractions file " + file, e);
        }
    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Attraction<?> attraction : attractions) {
                writer.write(String.join(";", attraction.getId(), attraction.getName(),
                        String.valueOf(attraction.getCapacity()), attraction.getType().name()));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new PersistenceException("Cannot write attractions file " + file, e);
        }
    }

    @Override
    public List<Attraction<?>> findAll() {
        return Collections.unmodifiableList(attractions);
    }

    @Override
    public Attraction<?> findById(String id) {
        for (Attraction<?> attraction : attractions) {
            if (attraction.getId().equals(id)) {
                return attraction;
            }
        }
        return null;
    }

    @Override
    public void add(Attraction<?> attraction) {
        attractions.add(attraction);
        save();
    }
}
