package es.upm.etsisi.amusementpark;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores attractions in a text file, one per line: Identifier;Name;Capacity.
 * Occupants are not stored, so every attraction loads empty.
 */
public class AttractionRepository {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 3;

    private final Path file;
    private final PrintStream warnings;

    public AttractionRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Attraction> load() throws ParkException {
        List<Attraction> attractions = new ArrayList<>();
        Set<String> identifiers = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFile.readLines(file)) {
            lineNumber++;
            if (!line.isBlank()) {
                String[] fields = line.split(SEPARATOR, -1);
                int capacity = fields.length == FIELD_COUNT ? Values.parseCapacity(fields[2]) : -1;
                if (capacity > 0 && !fields[0].isBlank() && identifiers.add(fields[0])) {
                    attractions.add(new Attraction(fields[0], fields[1], capacity));
                } else {
                    warnings.println("WARNING: skipping invalid attraction record at line " + lineNumber + " of " + file);
                }
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws ParkException {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(attraction.getIdentifier() + SEPARATOR + attraction.getName() + SEPARATOR
                    + attraction.getCapacity());
        }
        TextFile.writeLines(file, lines);
    }
}
