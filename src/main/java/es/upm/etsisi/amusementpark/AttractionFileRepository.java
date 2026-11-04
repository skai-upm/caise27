package es.upm.etsisi.amusementpark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attraction definitions in a text file, one per line:
 * {@code Id;Nombre;Capacidad}. Present users are never stored.
 */
public class AttractionFileRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 3;

    private final Path file;

    public AttractionFileRepository(Path file) {
        this.file = file;
    }

    /** Loads all attractions, empty; a missing file means there are none. */
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
                if (f.length != FIELD_COUNT) {
                    throw new IllegalArgumentException("wrong number of fields");
                }
                result.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                System.err.println("Skipping malformed attraction record at line " + lineNumber + " of " + file);
            }
        }
        return result;
    }

    /** Replaces the file content with the given attraction definitions. */
    public void save(Collection<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>(attractions.size());
        for (Attraction a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
