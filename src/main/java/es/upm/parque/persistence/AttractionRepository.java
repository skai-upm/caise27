package es.upm.parque.persistence;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;

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
 * Stores attractions in a text file, one per line: {@code id;name;capacity;type}. Visitors are not stored.
 * Lines without a type are read as GENERAL attractions.
 */
public class AttractionRepository {

    private static final String SEPARATOR = ";";

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction<?>> loadAll() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(SEPARATOR, -1);
                Attraction<?> attraction = parse(fields);
                if (attraction != null) {
                    attractions.add(attraction);
                }
            }
        }
        return attractions;
    }

    private static Attraction<?> parse(String[] fields) {
        if (fields.length != 3 && fields.length != 4) {
            return null;
        }
        AttractionType type = fields.length == 4 ? AttractionType.fromName(fields[3]) : AttractionType.GENERAL;
        try {
            return type == null ? null : type.create(fields[0], fields[1], Integer.parseInt(fields[2]));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void saveAll(Collection<Attraction<?>> attractions) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            for (Attraction<?> attraction : attractions) {
                writer.write(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                        Integer.toString(attraction.getCapacity()), attraction.getType().name()));
                writer.newLine();
            }
        }
    }
}
