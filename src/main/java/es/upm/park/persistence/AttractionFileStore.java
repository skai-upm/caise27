package es.upm.park.persistence;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.AttractionType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores attraction definitions, one per line: {@code Id;Nombre;Capacidad;Tipo} (lines without
 * the type, written before types existed, are GENERAL). Occupancy is
 * never stored, so loaded attractions are empty.
 */
public class AttractionFileStore implements AttractionRepository {

    private static final String SEPARATOR = ";";
    private static final int UNTYPED_FIELD_COUNT = 3;
    private static final int FIELD_COUNT = 4;

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    @Override
    public List<Attraction<?>> load() throws IOException {
        List<Attraction<?>> attractions = new ArrayList<>();
        if (!Files.exists(file)) {
            return attractions;
        }
        int lineNumber = 0;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = line.split(SEPARATOR, -1);
            boolean shape = f.length == FIELD_COUNT || f.length == UNTYPED_FIELD_COUNT;
            Integer capacity = shape ? Attraction.parseCapacity(f[2]) : null;
            Optional<AttractionType> type = !shape ? Optional.empty()
                    : f.length == UNTYPED_FIELD_COUNT ? Optional.of(AttractionType.GENERAL) : AttractionType.parse(f[3]);
            if (capacity == null || type.isEmpty() || f[0].isBlank() || f[1].isBlank()) {
                System.err.println("Skipping malformed attraction record at line " + lineNumber + " of " + file);
                continue;
            }
            attractions.add(type.get().create(f[0], f[1], capacity));
        }
        return attractions;
    }

    @Override
    public void save(List<Attraction<?>> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction<?> a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), Integer.toString(a.getCapacity()),
                    a.getType().name()));
        }
        Path absolute = file.toAbsolutePath();
        Path dir = absolute.getParent();
        if (dir != null) {
            Files.createDirectories(dir);
        }
        Path tmp = Files.createTempFile(dir, absolute.getFileName().toString(), ".tmp");
        try {
            Files.write(tmp, lines, StandardCharsets.UTF_8);
            try {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tmp, absolute, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tmp);
        }
    }
}
