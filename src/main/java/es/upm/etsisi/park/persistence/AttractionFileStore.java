package es.upm.etsisi.park.persistence;

import es.upm.etsisi.park.business.Attraction;
import es.upm.etsisi.park.business.AttractionType;
import es.upm.etsisi.park.business.ValueParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;type}; legacy lines without type are GENERAL.
 * Occupants are never stored.
 */
class AttractionFileStore {

    private static final int LEGACY_FIELDS = 3;
    private static final int FIELDS = 4;

    private final Path file;

    AttractionFileStore(Path file) {
        this.file = file;
    }

    Path getFile() {
        return file;
    }

    List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFile.readLines(file)) {
            String[] f = TextFile.fields(line);
            if (f.length != FIELDS && f.length != LEGACY_FIELDS) {
                TextFile.warnMalformed(file, line);
                continue;
            }
            AttractionType type = f.length == FIELDS ? AttractionType.parse(f[3]) : AttractionType.GENERAL;
            attractions.add(type.create(f[0], f[1], ValueParser.parseCapacity(f[2])));
        }
        return attractions;
    }

    List<String> toLines(Collection<Attraction<?>> attractions) {
        return attractions.stream()
                .map(a -> TextFile.join(a.getId(), a.getName(), Integer.toString(a.getCapacity()), a.getType().name()))
                .toList();
    }
}
