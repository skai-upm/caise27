package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.repository.AttractionRepository;

/**
 * Stores attraction definitions ({@code id;name;capacity;TYPE}); lines without
 * a type (written by Delivery 2) load as GENERAL. Users present are never
 * stored, so attractions start empty on every execution.
 */
public class FileAttractionRepository implements AttractionRepository {

    private static final String SEPARATOR = ";";

    private final Path file;

    public FileAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.split(SEPARATOR, -1);
            AttractionType type = parts.length == 4 ? AttractionType.fromName(parts[3]) : AttractionType.GENERAL;
            try {
                if (parts.length < 3 || parts.length > 4 || type == null) {
                    throw new NumberFormatException();
                }
                attractions.add(type.create(parts[0], parts[1], Integer.parseInt(parts[2])));
            } catch (NumberFormatException e) {
                System.err.println("WARNING: skipping malformed line " + lineNumber + " in " + file);
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        TextFiles.writeAtomically(file, lines);
    }
}
