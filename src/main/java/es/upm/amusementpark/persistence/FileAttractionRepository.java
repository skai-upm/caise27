package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.attraction.Attraction;
import es.upm.amusementpark.business.model.attraction.AttractionType;
import es.upm.amusementpark.business.repository.AttractionRepository;

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
 * Stores attractions as {@code id;name;capacity;TYPE}. Lines without a type (Delivery 2 files) load as
 * GENERAL. Occupants are never stored, so attractions load empty.
 */
public class FileAttractionRepository implements AttractionRepository {

    private static final String SEPARATOR = ";";
    private static final int LEGACY_FIELDS = 3;
    private static final int FIELDS = 4;

    private final Path path;
    private final PrintStream warnings;

    public FileAttractionRepository(Path path, PrintStream warnings) {
        this.path = path;
        this.warnings = warnings;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(path)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            AttractionType type = null;
            if (f.length == LEGACY_FIELDS) {
                type = AttractionType.GENERAL;
            } else if (f.length == FIELDS) {
                type = AttractionType.fromName(f[3]);
            }
            try {
                if (type == null) {
                    throw new NumberFormatException();
                }
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                warnings.println("WARNING: skipping malformed line " + lineNumber + " in " + path);
            }
        }
        return attractions;
    }

    @Override
    public void saveAll(Collection<Attraction<?>> attractions) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            for (Attraction<?> attraction : attractions) {
                writer.write(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                        String.valueOf(attraction.getCapacity()), attraction.getType().name()));
                writer.newLine();
            }
        }
    }
}
