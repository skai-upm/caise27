package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.business.model.Attraction;
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
        attractions = new AttractionManager(new AttractionStorage(dir.resolve("attractions.txt")));
        attractions.create("A01", "Tornado", "20");
    }

    @Test
    void createValidatesInput() {
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A01", "Otra", "5"));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Noria", "0"));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Noria", "-3"));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("A02", "Noria", "muchos"));
        assertThrows(IllegalArgumentException.class, () -> attractions.create("", "Noria", "5"));
        assertEquals(1, attractions.getAttractions().size());
        assertEquals("Tornado", attractions.find("A01").getName());
    }

    @Test
    void deleteOnlyEmptyAttractions() {
        attractions.enter("A01", new IndividualTicket("T001", DAY, BigDecimal.TEN, ana), DAY);
        assertThrows(IllegalArgumentException.class, () -> attractions.delete("A01"));
        attractions.removeUserEverywhere(ana);
        attractions.delete("A01");
        assertNull(attractions.find("A01"));
        assertThrows(IllegalArgumentException.class, () -> attractions.delete("A01"));
    }

    @Test
    void unknownAttractionIsReported() {
        IndividualTicket t = new IndividualTicket("T001", DAY, BigDecimal.TEN, ana);
        assertThrows(IllegalArgumentException.class, () -> attractions.enter("A99", t, DAY));
        assertThrows(IllegalArgumentException.class, () -> attractions.exit("A99", t));
    }

    @Test
    void restartKeepsDefinitionsButStartsEmpty() throws IOException {
        attractions.enter("A01", new IndividualTicket("T001", DAY, BigDecimal.TEN, ana), DAY);
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(dir.resolve("attractions.txt")));
        AttractionManager reloaded = new AttractionManager(new AttractionStorage(dir.resolve("attractions.txt")));
        Attraction a = reloaded.find("A01");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void malformedLinesAreSkipped() throws IOException {
        Path file = dir.resolve("other.txt");
        Files.write(file, List.of("A01;Tornado;x", "bad", "A02;Noria;8"));
        assertEquals(1, new AttractionStorage(file).load().size());
    }
}
