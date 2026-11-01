package es.upm.amusementpark.persistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import es.upm.amusementpark.business.model.Attraction;
import es.upm.amusementpark.business.model.AttractionType;
import es.upm.amusementpark.business.repository.AttractionRepository;

/**
 * Saves attraction definitions ({@code id;name;capacity;type}) to a text file and loads
 * them back; lines without a type (written before attraction types existed) load as
 * GENERAL. Users present in attractions are never stored.
 */
public class AttractionFileRepository implements AttractionRepository {

    private final Path file;

    public AttractionFileRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] f = line.split(";", -1);
            if (f.length != 3 && f.length != 4) {
                continue;
            }
            AttractionType type = f.length == 3 ? AttractionType.GENERAL : AttractionType.fromName(f[3]);
            if (type == null) {
                continue;
            }
            try {
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                // skip malformed line
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(a.getId() + ";" + a.getName() + ";" + a.getCapacity() + ";" + a.getType());
        }
        TextFiles.writeAtomically(file, lines);
    }
}
