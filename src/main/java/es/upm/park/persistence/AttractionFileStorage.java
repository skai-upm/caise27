package es.upm.park.persistence;

import es.upm.park.business.model.Attraction;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attraction definitions ({@code id;name;capacity}); occupancy is never stored.
 */
public class AttractionFileStorage {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 3;

    private final Path file;

    public AttractionFileStorage(Path file) {
        this.file = file;
    }

    public List<Attraction> load() {
        List<Attraction> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Attraction a = parse(line);
                if (a != null) {
                    result.add(a);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read attractions file " + file, e);
        }
        return result;
    }

    public void save(Collection<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity())));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write attractions file " + file, e);
        }
    }

    private static Attraction parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] f = line.split(SEPARATOR, -1);
        if (f.length != FIELD_COUNT || f[0].isBlank()) {
            return null;
        }
        try {
            int capacity = Integer.parseInt(f[2]);
            return capacity > 0 ? new Attraction(f[0], f[1], capacity) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
