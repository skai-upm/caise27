package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void roundTripWithoutVisitors() throws IOException {
        Path file = dir.resolve("data").resolve("attractions.txt");
        AttractionRepository repo = new AttractionRepository(file);
        assertTrue(repo.loadAll().isEmpty());
        repo.saveAll(List.of(new Attraction("A01", "Tornado", 20), new Attraction("A02", "Noria", 4)));
        assertEquals(List.of("A01;Tornado;20", "A02;Noria;4"), Files.readAllLines(file));
        List<Attraction> loaded = repo.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("Noria", loaded.get(1).getName());
        assertEquals(4, loaded.get(1).getCapacity());
        assertEquals(0, loaded.get(0).getOccupancy());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("attractions.txt");
        Files.write(file, List.of("A01;Tornado;veinte", "bad", "A02;Noria;4"));
        assertEquals(1, new AttractionRepository(file).loadAll().size());
    }
}
