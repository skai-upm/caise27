package es.upm.parque.persistence;

import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attraction definitions, one per line: {@code id;name;capacity;TYPE} (lines without a type,
 * from earlier versions, are GENERAL). The users present are never stored, so attractions are empty
 * at every start.
 */
public class AttractionRepository {

    private final Path file;

    public AttractionRepository(Path file) {
        this.file = file;
    }

    public List<Attraction<?>> loadAll() {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String line : TextFiles.readLines(file)) {
            String[] f = line.split(";", -1);
            if (f.length == 3 || f.length == 4) {
                try {
                    AttractionType type = f.length == 4 ? AttractionType.parse(f[3]) : AttractionType.GENERAL;
                    attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
                } catch (NumberFormatException | ParkException e) {
                    // malformed capacity or type: skip the line
                }
            }
        }
        return attractions;
    }

    public void saveAll(List<Attraction<?>> attractions) {
        TextFiles.writeLines(file, attractions.stream()
                .map(a -> a.getId() + ";" + a.getName() + ";" + a.getCapacity() + ";" + a.getType())
                .toList());
    }
}
