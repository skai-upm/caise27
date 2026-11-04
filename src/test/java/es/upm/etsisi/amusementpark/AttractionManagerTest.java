package es.upm.etsisi.amusementpark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AttractionManagerTest {

    @TempDir
    Path dir;

    @Test
    void createsAndPersistsDefinitionsOnly() throws Exception {
        Path file = dir.resolve("attractions.txt");
        AttractionManager m = new AttractionManager(new AttractionFileRepository(file));
        Attraction a = m.create("A01", "Tornado", "20");
        User ana = new User("Ana", "G", "12345678Z", "ana@correo.com", "Clave1!", "1", "1234567890123456");
        LocalDate day = LocalDate.of(2026, 10, 15);
        a.enter(new IndividualTicket("T001", day, ana, new BigDecimal("40.00")), day);

        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file, StandardCharsets.UTF_8));
        AttractionManager restarted = new AttractionManager(new AttractionFileRepository(file));
        Attraction loaded = restarted.require("A01");
        assertEquals("Tornado", loaded.getName());
        assertEquals(20, loaded.getCapacity());
        assertTrue(loaded.getPresent().isEmpty());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"A01|Otra|5", "' '|Tornado|20", "A02|''|20", "A02|Tornado|0",
        "A02|Tornado|-5", "A02|Tornado|veinte", "A02|Tornado|2.5", "A02|Tornado|''", "A02|Tornado|99999999999"})
    void invalidCreations(String id, String name, String capacity) throws Exception {
        Path file = dir.resolve("attractions.txt");
        AttractionManager m = new AttractionManager(new AttractionFileRepository(file));
        m.create("A01", "Tornado", "20");
        assertThrows(UserException.class, () -> m.create(id, name, capacity));
        assertEquals(1, m.getAttractions().size());
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file, StandardCharsets.UTF_8));
    }

    @Test
    void skipsMalformedLinesAndRollsBackOnSaveFailure() throws Exception {
        Path file = dir.resolve("attractions.txt");
        Files.write(file, List.of("A01;Tornado;20", "A02;Bad;x", "A03;Short", ""), StandardCharsets.UTF_8);
        assertEquals(1, new AttractionManager(new AttractionFileRepository(file)).getAttractions().size());

        Path plain = dir.resolve("plain");
        Files.writeString(plain, "x");
        AttractionManager broken = new AttractionManager(new AttractionFileRepository(plain.resolve("a.txt")));
        assertThrows(UserException.class, () -> broken.create("A01", "Tornado", "20"));
        assertTrue(broken.getAttractions().isEmpty());
        assertThrows(UserException.class, () -> broken.require("A01"));
    }
}
