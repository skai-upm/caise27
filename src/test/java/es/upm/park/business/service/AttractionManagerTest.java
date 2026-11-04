package es.upm.park.business.service;

import es.upm.park.business.model.Attraction;
import es.upm.park.business.model.IndividualTicket;
import es.upm.park.business.model.User;
import es.upm.park.persistence.AttractionFileStorage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.math.BigDecimal;
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
    void createPersistsDefinitionAndRestartIsEmpty() throws IOException {
        Path file = dir.resolve("attractions.txt");
        AttractionManager m = new AttractionManager(new AttractionFileStorage(file));
        Attraction a = m.create("A01", "Tornado", 20);
        User visitor = new User("U0", "S", "12345678Z", "u0@correo.com", "Clave1!", "1", "1234567890123456");
        a.enter(new IndividualTicket("T001", visitor, LocalDate.of(2026, 10, 15),
                new BigDecimal("40.00")), LocalDate.of(2026, 10, 15));
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file));

        AttractionManager again = new AttractionManager(new AttractionFileStorage(file));
        Attraction restored = again.find("A01");
        assertEquals("Tornado", restored.getName());
        assertEquals(20, restored.getCapacity());
        assertEquals(0, restored.getPresentCount());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {"''|Tornado|20", "A02|''|20", "A02|X|0", "A02|X|-3", "A01|Otro|5"})
    void invalidCreationRejected(String id, String name, int capacity) throws IOException {
        Path file = dir.resolve("attractions.txt");
        AttractionManager m = new AttractionManager(new AttractionFileStorage(file));
        m.create("A01", "Tornado", 20);
        assertThrows(IllegalArgumentException.class, () -> m.create(id, name, capacity));
        assertEquals(1, m.getAttractions().size());
        assertEquals(List.of("A01;Tornado;20"), Files.readAllLines(file));
    }
}
