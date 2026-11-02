package es.upm.etsisi.amusementpark.persistence;

import es.upm.etsisi.amusementpark.attraction.Attraction;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Text-file attraction storage: {@code id;name;capacity} per line. Occupants are never stored.
 */
public class AttractionFileRepository {

    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public AttractionFileRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFiles.fields(line);
            try {
                if (f.length != FIELD_COUNT) {
                    throw new IllegalArgumentException("wrong field count");
                }
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                warnings.println("WARNING: ignoring malformed attraction line in " + file);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        TextFiles.writeLines(file, attractions.stream().map(a -> String.join(TextFiles.SEPARATOR,
                a.getId(), a.getName(), Integer.toString(a.getCapacity()))).toList());
    }
}
