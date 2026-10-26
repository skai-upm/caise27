package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionManagerTest {

    @TempDir
    Path dir;

    private TestPark park;

    @BeforeEach
    void setUp() {
        park = new TestPark(dir);
        park.users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        park.users.create("Pedro", "Lopez", "87654321X", "pedro@correo.com", "Otra2#", "6", "9876543210987654");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 1));
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.groups.create("G01", "F");
        park.groups.addMember("G01", "pedro@correo.com");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.attractions.create("A01", "Tornado", "20");
    }

    @Test
    void createValidation() {
        assertNotNull(park.attractions.findById("A01"));
        assertThrows(ParkException.class, () -> park.attractions.create("A01", "Otra", "5"));
        assertThrows(ParkException.class, () -> park.attractions.create("A02", "Noria", "0"));
        assertThrows(ParkException.class, () -> park.attractions.create("A02", "Noria", "veinte"));
        assertThrows(ParkException.class, () -> park.attractions.create("A02", "", "5"));
        assertThrows(ParkException.class, () -> park.attractions.create(" ", "Noria", "5"));
        assertEquals(1, park.attractions.getAttractions().size());
    }

    @Test
    void entryRequiresTicketForToday() {
        assertThrows(ParkException.class, () -> park.attractions.enter("A01", "T001"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertEquals(1, park.attractions.enter("A01", "T001"));
        assertEquals(1, park.attractions.enter("A01", "TG01"));
        assertEquals(2, park.attractions.findById("A01").getOccupancy());
        assertThrows(ParkException.class, () -> park.attractions.enter("A99", "T001"));
        assertThrows(ParkException.class, () -> park.attractions.enter("A01", "T999"));
    }

    @Test
    void exitDoesNotCheckDate() {
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        park.attractions.enter("A01", "TG01");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 16));
        assertEquals(1, park.attractions.exit("A01", "TG01"));
        assertThrows(ParkException.class, () -> park.attractions.exit("A01", "TG01"));
        assertThrows(ParkException.class, () -> park.attractions.requireAttraction("A99"));
    }

    @Test
    void restartKeepsDefinitionsButNotVisitors() {
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        park.attractions.enter("A01", "T001");
        Attraction reloaded = new TestPark(dir).attractions.findById("A01");
        assertEquals("Tornado", reloaded.getName());
        assertEquals(20, reloaded.getCapacity());
        assertTrue(reloaded.getVisitors().isEmpty());
    }

    @Test
    void saveFailureRestoresPersistedState() {
        Path file = dir.resolve("attractions.txt");
        assertTrue(file.toFile().setWritable(false));
        try {
            assertThrows(ParkException.class, () -> park.attractions.create("A02", "Noria", "4"));
            assertNull(park.attractions.findById("A02"));
        } finally {
            assertTrue(file.toFile().setWritable(true));
        }
    }
}
