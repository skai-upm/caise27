package com.amusementpark.persistence;

import com.amusementpark.business.model.Attraction;
import com.amusementpark.business.model.AttractionType;
import com.amusementpark.business.validation.Validator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attractions as {@code id;name;capacity;type}. Users present are not stored; lines
 * without a type (written before attraction types existed) are read as GENERAL.
 */
public class FileAttractionRepository implements AttractionRepository {
    private final Path file;

    public FileAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String[] fields = line.split(";", -1);
            if (fields.length != 3 && fields.length != 4) {
                continue;
            }
            int capacity = Validator.parseCapacity(fields[2]);
            AttractionType type = fields.length == 3 ? AttractionType.GENERAL : AttractionType.fromName(fields[3]);
            if (capacity > 0 && type != null) {
                attractions.add(type.create(fields[0], fields[1], capacity));
            }
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> attraction : attractions) {
            lines.add(attraction.getId() + ";" + attraction.getName() + ";" + attraction.getCapacity() + ";"
                    + attraction.getType());
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }
}
