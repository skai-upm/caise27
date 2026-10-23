package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores attractions in a text file, one per line: {@code id;name;capacity;TYPE}.
 * Lines without a type (Delivery 2 files) are general attractions. Occupants
 * are never stored, so attractions always start empty.
 */
public class FileAttractionRepository implements AttractionRepository {

    public static final String DEFAULT_FILE_NAME = "attractions.txt";

    private static final String SEPARATOR = ";";
    private static final int UNTYPED_FIELD_COUNT = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;
    private final FileDataStore store;
    private final PrintStream warnings;

    public FileAttractionRepository(Path file, FileDataStore store, PrintStream warnings) {
        this.file = file;
        this.store = store;
        this.warnings = warnings;
    }


    @Override
    public List<Attraction<?>> loadAll() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        int lineNumber = 0;
        for (String line : TextFiles.readLines(file)) {
            lineNumber++;
            if (line.trim().isEmpty()) {
                continue;
            }
            Attraction<?> attraction = parse(line);
            if (attraction == null || !ids.add(attraction.getId())) {
                warnings.println("WARNING: skipping line " + lineNumber + " of " + file
                        + ": malformed or duplicate attraction");
            } else {
                attractions.add(attraction);
            }
        }
        return attractions;
    }

    @Override
    public void saveAll(Collection<Attraction<?>> attractions) throws IOException {
        store.write(file, toLines(attractions));
    }

    static List<String> toLines(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(String.join(SEPARATOR, attraction.getId(), attraction.getName(),
                    String.valueOf(attraction.getCapacity()), attraction.getType().name()));
        }
        return lines;
    }

    private static Attraction<?> parse(String line) {
        String[] fields = line.split(SEPARATOR, -1);
        boolean typed = fields.length == FIELD_COUNT;
        if ((!typed && fields.length != UNTYPED_FIELD_COUNT) || fields[0].trim().isEmpty()
                || fields[1].trim().isEmpty()) {
            return null;
        }
        try {
            int capacity = Integer.parseInt(fields[2].trim());
            AttractionType type = typed ? AttractionType.valueOf(fields[3].trim()) : AttractionType.GENERAL;
            return capacity > 0 ? type.create(fields[0].trim(), fields[1].trim(), capacity) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
