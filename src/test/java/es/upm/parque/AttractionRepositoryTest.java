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
    void savesAndLoadsDefinitionsOnly() throws IOException {
        Path file = dir.resolve("data").resolve("attractions.txt");
        AttractionRepository repository = new AttractionRepository(file);
        assertTrue(repository.load().isEmpty());
        Attraction tornado = new Attraction("A01", "Tornado", 20);
        tornado.admit(List.of(UserManagerTest.ana()));
        repository.save(List.of(tornado));
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file));
        Attraction loaded = repository.load().get(0);
        assertEquals("Tornado", loaded.getName());
        assertEquals(20, loaded.getCapacity());
        assertTrue(loaded.isEmpty());
    }

    @Test
    void ignoresMalformedLines() throws IOException {
        Path file = dir.resolve("attractions.txt");
        Files.write(file, List.of("roto", ";Sin id;5", "A02;Noria;0", "A03;Noria;x", "A04;Noria;8"));
        assertEquals(1, new AttractionRepository(file).load().size());
    }
}
