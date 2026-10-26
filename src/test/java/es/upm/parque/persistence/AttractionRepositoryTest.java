package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GeneralAttraction;
import es.upm.parque.business.model.GroupAttraction;
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
        repo.saveAll(List.of(new GeneralAttraction("A01", "Tornado", 20), new GroupAttraction("A02", "Noria", 4)));
        assertEquals(List.of("A01;Tornado;20;GENERAL", "A02;Noria;4;GROUP"), Files.readAllLines(file));
        List<Attraction<?>> loaded = repo.loadAll();
        assertEquals(2, loaded.size());
        assertEquals("Noria", loaded.get(1).getName());
        assertEquals(4, loaded.get(1).getCapacity());
        assertEquals(0, loaded.get(0).getOccupancy());
        assertEquals(AttractionType.GROUP, loaded.get(1).getType());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("attractions.txt");
        Files.write(file, List.of("A01;Tornado;veinte", "bad", "A02;Noria;4", "A03;X;4;VIP", "A04;Y;5;SPECIAL"));
        List<Attraction<?>> loaded = new AttractionRepository(file).loadAll();
        assertEquals(2, loaded.size());
        assertEquals(AttractionType.GENERAL, loaded.get(0).getType());
        assertEquals(AttractionType.SPECIAL, loaded.get(1).getType());
    }
}
