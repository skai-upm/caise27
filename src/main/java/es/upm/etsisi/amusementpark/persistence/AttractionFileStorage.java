package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.model.Attraction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attraction definitions, one per line: {@code Id;Nombre;Capacidad}. Users present are not stored:
 * attractions are always empty when the application starts. Only class touching the attraction file.
 */
public class AttractionFileStorage implements AttractionRepository {

    public static final String DEFAULT_FILE = "attractions.txt";
    private static final String SEPARATOR = ";";
    private static final int FIELDS = 3;

    private final Path file;

    public AttractionFileStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read attraction file " + file, e);
        }
        for (String line : lines) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != FIELDS) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping malformed attraction record: " + line);
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction> attractions) {
        List<String> lines = new ArrayList<>(attractions.size());
        for (Attraction a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write attraction file " + file, e);
        }
    }
}
