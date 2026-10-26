package es.upm.parque.persistence;

import es.upm.parque.business.model.Attraction;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attraction definitions, one per line: {@code id;name;capacity}. The users present are
 * never stored, so attractions are empty at every start.
 */
public class AttractionRepository {

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction> loadAll() {
        List<Attraction> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = line.split(";", -1);
            if (f.length == 3) {
                try {
                    attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
                } catch (NumberFormatException e) {
                    // malformed capacity: skip the line
                }
            }
        }
        return attractions;
    }

    public void saveAll(List<Attraction> attractions) {
        TextFiles.writeLines(file, attractions.stream()
                .map(a -> a.getId() + ";" + a.getName() + ";" + a.getCapacity())
                .toList());
    }
}
