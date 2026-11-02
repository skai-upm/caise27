package com.amusementpark.persistence;

import com.amusementpark.business.model.Attraction;
import com.amusementpark.business.model.AttractionType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists attraction definitions as {@code id;name;capacity;type} (E2 lines without type load as
 * GENERAL). Visitors are never stored.
 */
public class AttractionFileRepository implements AttractionRepository {

    private final LineFile file;

    public AttractionFileRepository(Path file) {
        this.file = new LineFile(file);
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        for (String[] f : file.read(3, 4)) {
            AttractionType type = f.length > 3 ? AttractionType.fromText(f[3]) : AttractionType.GENERAL;
            if (type == null) {
                System.err.println("WARNING: skipping attraction with unknown type: " + f[0]);
                continue;
            }
            try {
                attractions.add(type.create(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                System.err.println("WARNING: skipping attraction with invalid capacity: " + f[0]);
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(LineFile.join(a.getId(), a.getName(), Integer.toString(a.getCapacity()), a.getType().name()));
        }
        file.write(lines);
    }
}
