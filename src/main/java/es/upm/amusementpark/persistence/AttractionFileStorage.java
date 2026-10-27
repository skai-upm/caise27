package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Reads and writes attractions, one per line: {@code id;name;capacity;type}.
 * Lines without a type are general attractions. The users inside attractions
 * are not stored.
 */
public class AttractionFileStorage {

    private final Path file;
    private int skippedLines;

    public AttractionFileStorage(Path file) {
        this.file = file;
    }

    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        skippedLines = 0;
        for (String[] f : TextFiles.readRecords(file)) {
            AttractionType type = f.length == 3 ? AttractionType.GENERAL
                    : f.length == 4 ? AttractionType.fromName(f[3]) : null;
            try {
                if (type == null) {
                    throw new NumberFormatException();
                }
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                skippedLines++;
            }
        }
        return attractions;
    }

    public void save(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(a.getIdentifier() + ";" + a.getName() + ";" + a.getCapacity() + ";" + a.getType());
        }
        TextFiles.writeLines(file, lines);
    }

    public int getSkippedLines() {
        return skippedLines;
    }
}
