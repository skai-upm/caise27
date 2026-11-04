package com.parque.persistence;

import com.parque.business.model.Attraction;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attraction definitions, one per line: {@code Identificador;Nombre;Aforo}. Occupancy is not stored.
 */
public class AttractionRepository {
    private static final String SEPARATOR = ";";

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != 3) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                result.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping malformed attraction line " + lineNumber + " in " + file);
            }
        }
        return result;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(a.getId() + SEPARATOR + a.getName() + SEPARATOR + a.getCapacity());
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
