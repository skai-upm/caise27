package es.upm.etsisi.park.persistence;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.etsisi.park.model.attraction.Attraction;

/**
 * Stores attractions one per line: {@code id;name;capacity}. Occupants are
 * not stored, so every attraction is empty after loading.
 */
public class AttractionRepository {

    public static final String FILE_NAME = "attractions.txt";

    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public AttractionRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        List<String> lines = TextFiles.readLines(file);
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) {
                continue;
            }
            String[] fields = TextFiles.fields(line);
            try {
                if (fields.length != FIELD_COUNT) {
                    throw new IllegalArgumentException("expected " + FIELD_COUNT + " fields");
                }
                attractions.add(new Attraction(fields[0], fields[1], Integer.parseInt(fields[2])));
            } catch (IllegalArgumentException e) {
                warnings.println("WARNING: skipping malformed line " + (i + 1) + " in " + file);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(String.join(TextFiles.SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity())));
        }
        TextFiles.write(file, lines);
    }
}
