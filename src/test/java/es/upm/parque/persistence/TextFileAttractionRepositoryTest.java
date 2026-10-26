package es.upm.parque.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import es.upm.parque.Fixtures;
import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GeneralAttraction;
import es.upm.parque.business.model.SpecialAttraction;

class TextFileAttractionRepositoryTest {

    @TempDir
    Path dir;

    @Test
    void savesAndLoadsDefinitionsOnly() throws IOException {
        Path file = dir.resolve("data").resolve("attractions.txt");
        AttractionRepository repository = new TextFileAttractionRepository(file);
        assertTrue(repository.load().isEmpty());
        Attraction<?> tornado = new GeneralAttraction("A01", "Tornado", 20);
        tornado.admit(List.of(Fixtures.ana()));
        repository.save(List.of(tornado));
        assertEquals(List.of("A01;Tornado;20;GENERAL"), Files.readAllLines(file));
        Attraction<?> loaded = repository.load().get(0);
        assertEquals("Tornado", loaded.getName());
        assertEquals(20, loaded.getCapacity());
        assertTrue(loaded.isEmpty());
    }

    @Test
    void ignoresMalformedLines() throws IOException {
        Path file = dir.resolve("attractions.txt");
        Files.write(file, List.of("roto", ";Sin id;5", "A02;Noria;0", "A03;Noria;x", "A04;Noria;8", "A05;VIP;2;OTHER",
                "A06;VIP;2;SPECIAL"));
        List<Attraction<?>> loaded = new TextFileAttractionRepository(file).load();
        assertEquals(2, loaded.size());
        assertEquals(AttractionType.GENERAL, loaded.get(0).getType());
        assertEquals(AttractionType.SPECIAL, loaded.get(1).getType());
    }

    @Test
    void storesTheType() throws IOException {
        Path file = dir.resolve("attractions.txt");
        new TextFileAttractionRepository(file).save(List.of(new SpecialAttraction("A01", "VIP", 2)));
        assertEquals(List.of("A01;VIP;2;SPECIAL"), Files.readAllLines(file));
        assertEquals(AttractionType.SPECIAL, new TextFileAttractionRepository(file).load().get(0).getType());
    }
}
