package es.upm.parque;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores attractions as {@code id;name;capacity}. Users present are never stored:
 * attractions are empty when the application starts.
 */
public class AttractionRepository {

    private static final int FIELDS = 3;

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        return TextFile.readRecords(file, FIELDS).stream()
                .map(f -> new Attraction(f[0], f[1], Integer.parseInt(f[2])))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void save(Collection<Attraction> attractions) {
        TextFile.writeRecords(file, attractions.stream()
                .map(a -> TextFile.join(a.getId(), a.getName(), String.valueOf(a.getCapacity())))
                .toList());
    }
}
