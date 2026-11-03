package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.Attraction;
import es.upm.etsisi.park.business.AttractionType;
import es.upm.etsisi.park.business.GeneralAccess;
import es.upm.etsisi.park.business.ValidationException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;type}. Lines without a type (from
 * Delivery 2) are read as GENERAL. Occupants are never stored.
 */
public class AttractionRepository extends TextFileRepository {

    private static final int LEGACY_FIELDS = 3;
    private static final int FIELDS = 4;

    public AttractionRepository(Path file) {
        super(file);
    }

    public List<Attraction<? extends GeneralAccess>> load() throws IOException {
        List<Attraction<? extends GeneralAccess>> attractions = new ArrayList<>();
        for (String line : readLines()) {
            String[] f = line.split(SEPARATOR, -1);
            int capacity = f.length == LEGACY_FIELDS || f.length == FIELDS ? parseCapacity(f[2]) : -1;
            AttractionType type = capacity <= 0 ? null : parseType(f);
            if (type == null) {
                warnMalformed(line);
                continue;
            }
            attractions.add(type.create(f[0], f[1], capacity));
        }
        return attractions;
    }

    public void saveAll(Collection<Attraction<? extends GeneralAccess>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<? extends GeneralAccess> a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity()),
                    a.getType().name()));
        }
        writeLines(lines);
    }

    private static AttractionType parseType(String[] f) {
        if (f.length == LEGACY_FIELDS) {
            return AttractionType.GENERAL;
        }
        try {
            return AttractionType.parse(f[3]);
        } catch (ValidationException e) {
            return null;
        }
    }

    /** Returns the capacity, or -1 if the text is not an integer. */
    private static int parseCapacity(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
