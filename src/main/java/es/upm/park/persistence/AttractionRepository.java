package es.upm.park.persistence;

import es.upm.park.model.Attraction;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/** Stores attractions without occupants: {@code id;name;capacity}. */
public class AttractionRepository {

    private final Path file;
    private final PrintStream warnings;

    public AttractionRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Attraction> loadAll() {
        return TextFile.parseAll(file, fields -> {
            TextFile.requireFields(fields, 3);
            return new Attraction(fields[0], fields[1], Integer.parseInt(fields[2]));
        }, warnings);
    }

    public void saveAll(List<Attraction> attractions) {
        TextFile.writeLines(file, attractions.stream()
                .map(a -> a.getId() + TextFile.SEPARATOR + a.getName() + TextFile.SEPARATOR + a.getCapacity())
                .toList());
    }
}
