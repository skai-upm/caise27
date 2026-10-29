package es.upm.amusementpark.persistence;

import es.upm.amusementpark.business.exception.ParkException;
import es.upm.amusementpark.business.repository.AttractionRepository;

import es.upm.amusementpark.business.control.ParkController;
import es.upm.amusementpark.business.model.Attraction;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Persists attractions as {@code id;name;capacity} lines (occupants are not stored). */
public class TextFileAttractionRepository extends TextFileRepository
        implements AttractionRepository {

    public TextFileAttractionRepository(Path file) {
        super(file);
    }

    @Override
    public void saveAll(Collection<Attraction> attractions) {
        List<String> lines = new ArrayList<>();
        for (Attraction attraction : attractions) {
            lines.add(join(attraction.getIdentifier(), attraction.getName(), attraction.getCapacity()));
        }
        writeLines(lines);
    }

    @Override
    public void loadInto(ParkController park, List<String> skipped) {
        for (String line : readLines()) {
            if (line.isBlank()) {
                continue;
            }
            String[] f = fields(line);
            try {
                if (f.length == 3) {
                    park.createAttraction(f[0], f[1], f[2]);
                } else {
                    skipped.add(line);
                }
            } catch (ParkException e) {
                skipped.add(line);
            }
        }
    }
}
