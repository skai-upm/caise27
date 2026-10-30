package es.upm.etsisi.amusementpark.repository;

import es.upm.etsisi.amusementpark.model.Attraction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity}. Occupants are never stored.
 */
public class AttractionFileStore {

    private final LineFile file;

    public AttractionFileStore(Path file) {
        this.file = new LineFile(file);
    }

    public List<Attraction> load() {
        List<Attraction> attractions = new ArrayList<>();
        for (String[] f : file.readRecords()) {
            try {
                if (f.length != 3) {
                    throw new IllegalArgumentException();
                }
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (IllegalArgumentException e) {
                file.warnMalformed(f);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(LineFile.join(a.getId(), a.getName(), a.getCapacity()));
        }
        file.writeLines(lines);
    }
}
