package es.upm.park.persistence;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;
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
 * Stores attraction definitions ({@code id;name;capacity;type}); occupancy is never stored.
 */
public class AttractionFileStorage {

    private static final String SEPARATOR = ";";
    private static final int LEGACY_FIELD_COUNT = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;

    public AttractionFileStorage(Path file) {
        this.file = file;
    }

    public List<Attraction<?>> load() {
        List<Attraction<?>> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                Attraction<?> a = parse(line);
                if (a != null) {
                    result.add(a);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read attractions file " + file, e);
        }
        return result;
    }

    public void save(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity()),
                    a.type().name()));
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

    /** Reads {@code id;name;capacity;type}; legacy lines without type are GENERAL. */
    private static Attraction<?> parse(String line) {
        if (line == null || line.isBlank()) {
            return null;
        }
        String[] f = line.split(SEPARATOR, -1);
        if ((f.length != FIELD_COUNT && f.length != LEGACY_FIELD_COUNT) || f[0].isBlank()) {
            return null;
        }
        try {
            int capacity = Integer.parseInt(f[2]);
            AttractionType type = f.length == LEGACY_FIELD_COUNT ? AttractionType.GENERAL : AttractionType.parse(f[3]);
            return capacity > 0 ? type.create(f[0], f[1], capacity) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
