package com.amusementpark;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists attraction definitions as {@code id;name;capacity}. Visitors are never stored.
 */
public class AttractionFileStorage {

    private final LineFile file;

    public AttractionFileStorage(Path file) {
        this.file = new LineFile(file);
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
        for (String[] f : file.read(3)) {
            try {
                attractions.add(new Attraction(f[0], f[1], Integer.parseInt(f[2])));
            } catch (NumberFormatException e) {
                System.err.println("WARNING: skipping attraction with invalid capacity: " + f[0]);
            }
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(LineFile.join(a.getId(), a.getName(), Integer.toString(a.getCapacity())));
        }
        file.write(lines);
    }
}
