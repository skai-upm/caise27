package es.upm.etsisi.park;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored.
 */
public class AttractionRepository {

    private static final int FIELDS = 3;

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public Path getFile() {
        return file;
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFile.readLines(file)) {
            String[] f = TextFile.fields(line);
            if (f.length != FIELDS) {
                TextFile.warnMalformed(file, line);
                continue;
            }
            attractions.add(new Attraction(f[0], f[1], ValueParser.parseCapacity(f[2])));
        }
        return attractions;
    }

    List<String> toLines(Collection<Attraction> attractions) {
        return attractions.stream()
                .map(a -> TextFile.join(a.getId(), a.getName(), Integer.toString(a.getCapacity())))
                .toList();
    }
}
