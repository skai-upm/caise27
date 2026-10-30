package es.upm.amusementpark.business.controller;

import es.upm.amusementpark.business.model.Attraction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTest {

    @TempDir
    Path dir;

    private ParkTestSupport s;

    @BeforeEach
    void setUp() throws IOException {
        s = new ParkTestSupport(dir);
        s.run(ParkTestSupport.ANA, "SYSTEM DATE;2026-10-15", "TICKET CREATE;T001;ana@correo.com;2026-10-15");
    }

    private Attraction attraction(String id) {
        return s.park().getAttractionService().findById(id);
    }

    @Test
    void createAttraction() {
        assertTrue(s.run("ATTRACTION CREATE;A01;Tornado;20").startsWith("OK"));
        Attraction a = attraction("A01");
        assertEquals("Tornado", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getPresent().isEmpty());
    }

    @Test
    void invalidAttractions() {
        s.run("ATTRACTION CREATE;A01;Tornado;20");
        assertTrue(s.run("ATTRACTION CREATE;A02;Rio;0").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION CREATE;A02;Rio;abc").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION CREATE;A02;;5").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR"));
        assertEquals(1, s.park().getAttractionService().getAttractions().size());
    }

    @Test
    void individualEntryExitAndStatus() {
        s.createGroup("G01", 3, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "ATTRACTION CREATE;A01;Tornado;20");
        assertTrue(s.run("ATTRACTION ENTER;A01;T001").startsWith("OK"));
        assertTrue(s.run("ATTRACTION ENTER;A01;TG01").startsWith("OK"));
        String status = s.run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Tornado"));
        assertTrue(status.contains("Capacity: 20"));
        assertTrue(status.contains("Occupancy: 4"));
        assertTrue(status.contains("Available: 16"));
        assertTrue(status.contains("ana@correo.com"));
        assertTrue(status.contains("user3@correo.com"));
        assertTrue(s.run("ATTRACTION EXIT;A01;T001").startsWith("OK"));
        status = s.run("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Occupancy: 3"));
        assertTrue(status.contains("Available: 17"));
        assertTrue(!status.contains("ana@correo.com"));
    }

    @Test
    void groupEntryIsAtomicOnCapacity() {
        s.createGroup("G01", 4, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "ATTRACTION CREATE;A01;Tornado;4",
                "ATTRACTION ENTER;A01;T001");
        assertTrue(s.run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertEquals(1, attraction("A01").getPresent().size());
    }

    @Test
    void ticketMustBeForToday() {
        s.run("ATTRACTION CREATE;A01;Tornado;20", "TICKET CREATE;T002;ana@correo.com;2026-10-16");
        assertTrue(s.run("ATTRACTION ENTER;A01;T002").startsWith("ERROR"));
        s.run("SYSTEM DATE;2026-10-16");
        assertTrue(s.run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertTrue(attraction("A01").getPresent().isEmpty());
    }

    @Test
    void alreadyPresentUserCannotEnterTwice() {
        s.run("ATTRACTION CREATE;A01;Tornado;20", "TICKET CREATE;T002;ana@correo.com;2026-10-15",
                "ATTRACTION ENTER;A01;T001");
        assertTrue(s.run("ATTRACTION ENTER;A01;T002").startsWith("ERROR"));
        assertEquals(1, attraction("A01").getPresent().size());
    }

    @Test
    void exitAfterDateChangeWithoutRevalidation() {
        s.run("ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION ENTER;A01;T001", "SYSTEM DATE;2026-10-20");
        assertTrue(s.run("ATTRACTION EXIT;A01;T001").startsWith("OK"));
        assertEquals(20, attraction("A01").getAvailable());
        assertTrue(s.run("ATTRACTION EXIT;A01;T001").startsWith("ERROR"));
    }

    @Test
    void memberCanExitWithConstituentTicket() {
        s.createGroup("G01", 2, 1);
        s.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "ATTRACTION CREATE;A01;Tornado;20",
                "ATTRACTION ENTER;A01;TG01");
        assertTrue(s.run("ATTRACTION EXIT;A01;TG01-1").startsWith("OK"));
        assertEquals(1, attraction("A01").getPresent().size());
    }

    @Test
    void unknownReferences() {
        s.run("ATTRACTION CREATE;A01;Tornado;20");
        assertTrue(s.run("ATTRACTION ENTER;A99;T001").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION ENTER;A01;T999").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION EXIT;A99;T001").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION EXIT;A01;T999").startsWith("ERROR"));
        assertTrue(s.run("ATTRACTION STATUS;A99").startsWith("ERROR"));
    }

    @Test
    void attractionsRestartEmpty() throws IOException {
        s.run("ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION ENTER;A01;T001");
        s.restart();
        Attraction a = attraction("A01");
        assertEquals(20, a.getCapacity());
        assertTrue(a.getPresent().isEmpty());
        assertTrue(s.run("ATTRACTION STATUS;A01").contains("Users present: none"));
    }
}
