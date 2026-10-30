package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attraction definitions as {@code id;name;capacity;type} (records without type load as GENERAL).
 * Users present are never stored.
 */
public class FileAttractionRepository implements AttractionRepository {

    private final Path file;

    public FileAttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] f = line.split(";", -1);
            AttractionType type = f.length == 4 ? AttractionType.parse(f[3]) : AttractionType.GENERAL;
            try {
                if (type == null || f.length < 3 || f.length > 4) {
                    throw new IllegalArgumentException("invalid record");
                }
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (RuntimeException e) {
                System.err.println("WARNING: skipping malformed attraction record: " + line);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(a.getId() + ";" + a.getName() + ";" + a.getCapacity() + ";" + a.getType());
        }
        TextFiles.write(file, lines);
    }
}
