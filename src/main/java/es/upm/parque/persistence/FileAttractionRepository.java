package es.upm.parque.persistence;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.repository.AttractionRepository;

/**
 * Stores attractions as {@code id;name;capacity;type}. Lines without type (Delivery 2) are GENERAL.
 * Users present are never stored: attractions are empty when the application starts.
 */
public class FileAttractionRepository implements AttractionRepository {

    private static final int LEGACY_FIELDS = 3;
    private static final int FIELDS = 4;

    private final Path file;

    public FileAttractionRepository(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() {
        return TextFile.readRecords(file, LEGACY_FIELDS, FIELDS).stream()
                .map(f -> (f.length == FIELDS ? AttractionType.fromText(f[3]) : AttractionType.GENERAL)
                        .create(f[0], f[1], Integer.parseInt(f[2])))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void save(Collection<Attraction<?>> attractions) {
        TextFile.writeRecords(file, attractions.stream()
                .map(a -> TextFile.join(a.getId(), a.getName(), String.valueOf(a.getCapacity()),
                        a.getType().name()))
                .toList());
    }
}
