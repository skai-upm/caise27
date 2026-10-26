package es.upm.parque.persistence;

import es.upm.parque.business.model.Attraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions in a text file, one per line: {@code Id;Nombre;Capacidad}. Users present are not stored.
 */
public class AttractionRepository {
    private static final String SEPARATOR = ";";

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] f = line.split(SEPARATOR, -1);
                try {
                    if (f.length != 3) {
                        throw new NumberFormatException();
                    }
                    attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
                } catch (NumberFormatException e) {
                    System.err.println("Ignoring malformed attraction line: " + line);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read attractions file " + file + ": " + e.getMessage(), e);
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) {
        try {
            if (file.getParent() != null) {
                Files.createDirectories(file.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (Attraction a : attractions) {
                    writer.write(a.getId() + SEPARATOR + a.getName() + SEPARATOR + a.getCapacity());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write attractions file " + file + ": " + e.getMessage(), e);
        }
    }
}
