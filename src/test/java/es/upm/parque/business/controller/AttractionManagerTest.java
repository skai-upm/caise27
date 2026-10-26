package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Attraction;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;
import es.upm.parque.persistence.AttractionStorage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionManagerTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;

    private AttractionManager attractions;
    private final User ana =
        new User("Ana", "Garcia Lopez", "12345678Z", "ana@correo.com", "Clave1!", "612345678", "1234567890123456");

    @BeforeEach
    void setUp() {
        attractions = AttractionManager.getInstance().configure(new AttractionStorage(dir.resolve("attractions.txt")));
        attractions.create("A01", "Tornado", "20", AttractionType.GENERAL);
    }

    @Test
    void createValidatesInput() {
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A01", "Otra", "5", AttractionType.GENERAL));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Noria", "0", AttractionType.GENERAL));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Noria", "-3", AttractionType.GENERAL));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Noria", "muchos", AttractionType.GENERAL));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("", "Noria", "5", AttractionType.GENERAL));
        assertEquals(1, attractions.getAttractions().size());
        assertEquals("Tornado", attractions.find("A01").getName());
    }

    @Test
    void deleteOnlyEmptyAttractions() {
        attractions.enter("A01", new IndividualTicket("T001", DAY, BigDecimal.TEN, ana), DAY, false);
        assertThrows(IllegalArgumentException.class, () -> attractions.delete("A01"));
        attractions.removeUserEverywhere(ana);
        attractions.delete("A01");
        assertNull(attractions.find("A01"));
        assertThrows(IllegalArgumentException.class, () -> attractions.delete("A01"));
    }

    @Test
    void unknownAttractionIsReported() {
        IndividualTicket t = new IndividualTicket("T001", DAY, BigDecimal.TEN, ana);
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A99", t, DAY, false));
        assertThrows(IllegalArgumentException.class, () -> attractions.exit("A99", t));
    }

    @Test
    void restartKeepsDefinitionsButStartsEmpty() throws IOException {
        attractions.enter("A01", new IndividualTicket("T001", DAY, BigDecimal.TEN, ana), DAY, false);
        assertEquals(List.of("A01;Tornado;20;GENERAL"), Files.readAllLines(dir.resolve("attractions.txt")));
        AttractionManager reloaded = AttractionManager.getInstance().configure(new AttractionStorage(dir.resolve("attractions.txt")));
        Attraction<?> a = reloaded.find("A01");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("other.txt");
        Files.write(file, List.of("A01;Tornado;x", "bad", "A02;Noria;8"));
        assertEquals(1, new AttractionStorage(file).load().size());
    }

    @Test
    void typesArePersistedAndLegacyLinesAreGeneral() throws IOException {
        attractions.create("A02", "Carrusel", "10", AttractionType.SPECIAL);
        AttractionManager reloaded = AttractionManager.getInstance()
            .configure(new AttractionStorage(dir.resolve("attractions.txt")));
        assertEquals(AttractionType.SPECIAL, reloaded.find("A02").getType());
        Path legacy = Files.write(dir.resolve("legacy.txt"), List.of("A09;Noria;8"));
        assertEquals(AttractionType.GENERAL, new AttractionStorage(legacy).load().get(0).getType());
    }
}
