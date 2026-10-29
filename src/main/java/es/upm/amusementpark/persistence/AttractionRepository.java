package es.upm.amusementpark.persistence;

import es.upm.amusementpark.model.Attraction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored, so attractions load empty.
 */
public class AttractionRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELDS = 3;

    private final Path path;
    private final PrintStream warnings;

    public AttractionRepository(Path path, PrintStream warnings) {
        this.path = path;
        this.warnings = warnings;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(path)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            try {
                if (f.length != FIELDS) {
                    throw new NumberFormatException();
                }
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + path);
            }
        }
        return attractions;
    }

    public void saveAll(Collection<Attraction> attractions) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Attraction attraction : attractions) {
                writer.write(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                        String.valueOf(attraction.getCapacity())));
                writer.newLine();
            }
        }
    }
}
