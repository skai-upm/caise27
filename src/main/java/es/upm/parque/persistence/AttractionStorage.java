package es.upm.parque.persistence;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Stores attraction definitions in a text file, one per line: {@code Id;Nombre;Capacidad;Tipo}. Lines without
 * type (written by older versions) are GENERAL attractions.
 * Users present are not stored: attractions always start empty.
 */
public class AttractionStorage implements AttractionRepository {

    private static final String SEPARATOR = ";";

    private final Path file;

    public AttractionStorage(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() {
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String[] f = line.split(SEPARATOR, -1);
                if ((f.length == 3 || f.length == 4) && !f[0].isBlank()) {
                    try {
                        AttractionType type = f.length == 4 ? AttractionType.valueOf(f[3]) : AttractionType.GENERAL;
                        attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
                    } catch (IllegalArgumentException e) {
                        // malformed line: skipped
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read attraction file " + file, e);
        }
        return attractions;
    }

    @Override
    public void save(Collection<Attraction<?>> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), String.valueOf(a.getCapacity()),
                a.getType().name()));
        }
        try {
            Path parent = file.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot write attraction file " + file, e);
        }
    }
}
