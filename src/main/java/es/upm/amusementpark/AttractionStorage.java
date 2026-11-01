package es.upm.amusementpark;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves attraction definitions ({@code id;name;capacity}) to a text file and loads
 * them back. Users present in attractions are never stored.
 */
public class AttractionStorage {

    private final Path file;

    public AttractionStorage(Path file) {
        this.file = file;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] f = line.split(";", -1);
            if (f.length != 3) {
                continue;
            }
            try {
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                // skip malformed line
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(a.getId() + ";" + a.getName() + ";" + a.getCapacity());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
