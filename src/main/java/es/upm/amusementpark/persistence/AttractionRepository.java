package es.upm.amusementpark.persistence;

import es.upm.amusementpark.park.Attraction;
import es.upm.amusementpark.park.ParkController;
import es.upm.amusementpark.park.ParkException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists attractions as {@code id;name;capacity}. Occupants are never persisted.
 */
public class AttractionRepository {

    private final TextFile file;

    public AttractionRepository(Path path) {
        this.file = new TextFile(path);
    }

    public void load(ParkController park, List<String> warnings) throws IOException {
        List<String> lines = file.readLines();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            String[] f = TextFile.fields(line);
            try {
                if (f.length != 3) {
                    throw new IllegalArgumentException("expected 3 fields");
                }
                park.createAttraction(f[0], f[1], Integer.parseInt(f[2]));
            } catch (IllegalArgumentException | ParkException e) {
                warnings.add(file.getPath().getFileName() + " line " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    public void save(ParkController park) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : park.getAttractions()) {
            lines.add(TextFile.join(a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        file.writeLines(lines);
    }
}
