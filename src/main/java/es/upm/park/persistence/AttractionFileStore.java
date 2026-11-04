package es.upm.park.persistence;

import es.upm.park.business.model.Attraction;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores attraction definitions, one per line: {@code Id;Nombre;Capacidad}. Occupancy is
 * never stored, so loaded attractions are empty.
 */
public class AttractionFileStore {

    private static final String SEPARATOR = ";";
    private static final int FIELD_COUNT = 3;

    private final Path file;

    public AttractionFileStore(Path file) {
        this.file = file;
    }

    public List<Attraction> load() throws IOException {
        List<Attraction> attractions = new ArrayList<>();
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
            Integer capacity = f.length == FIELD_COUNT ? Attraction.parseCapacity(f[2]) : null;
            if (capacity == null || f[0].isBlank() || f[1].isBlank()) {
                System.err.println("Skipping malformed attraction record at line " + lineNumber + " of " + file);
                continue;
            }
            attractions.add(new Attraction(f[0], f[1], capacity));
        }
        return attractions;
    }

    public void save(List<Attraction> attractions) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Attraction a : attractions) {
            lines.add(String.join(SEPARATOR, a.getId(), a.getName(), Integer.toString(a.getCapacity())));
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
