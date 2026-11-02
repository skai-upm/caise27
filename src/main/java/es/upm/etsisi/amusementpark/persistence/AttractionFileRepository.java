package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.business.attraction.Attraction;
import es.upm.etsisi.amusementpark.business.attraction.AttractionType;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Text-file attraction storage: {@code id;name;capacity;TYPE} per line. Lines without a type
 * (Delivery 2 data) are read as GENERAL. Occupants are never stored.
 */
public class AttractionFileRepository {

    private static final int LEGACY_FIELDS = 3;
    private static final int FIELDS = 4;

    private final Path file;
    private final PrintStream warnings;

    public AttractionFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            try {
                AttractionType type = typeOf(f);
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                warnings.println("WARNING: ignoring malformed attraction line in " + file);
            }
        }
        return attractions;
    }

    public void save(List<Attraction<?>> attractions) throws IOException {
        TextFiles.writeLines(file, attractions.stream().map(a -> String.join(TextFiles.SEPARATOR,
                a.getId(), a.getName(), Integer.toString(a.getCapacity()), a.getType().name())).toList());
    }

    private static AttractionType typeOf(String[] f) {
        if (f.length == LEGACY_FIELDS) {
            return AttractionType.GENERAL;
        }
        if (f.length != FIELDS) {
            throw new IllegalArgumentException("wrong field count");
        }
        return AttractionType.fromName(f[3]).orElseThrow(() -> new IllegalArgumentException("unknown type"));
    }
}
