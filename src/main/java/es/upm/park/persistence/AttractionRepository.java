package es.upm.park.persistence;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.List;

/** Stores attractions without occupants: {@code id;name;capacity;type} (no type means GENERAL). */
public class AttractionRepository {

    private final Path file;
    private final PrintStream warnings;

    public AttractionRepository(Path file, PrintStream warnings) {
        this.file = file;
        this.warnings = warnings;
    }

    public List<Attraction<?>> loadAll() {
        return TextFile.parseAll(file, fields -> {
            TextFile.requireFields(fields, 3);
            AttractionType type = fields.length > 3 ? AttractionType.valueOf(fields[3]) : AttractionType.GENERAL;
            return type.create(fields[0], fields[1], Integer.parseInt(fields[2]));
        }, warnings);
    }

    public void saveAll(List<Attraction<?>> attractions) {
        TextFile.writeLines(file, attractions.stream()
                .map(a -> String.join(TextFile.SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity()),
                        a.getType().name()))
                .toList());
    }
}
