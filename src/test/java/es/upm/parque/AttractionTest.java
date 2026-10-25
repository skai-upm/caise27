package es.upm.parque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AttractionTest {

    private static final LocalDate DAY = LocalDate.of(2026, 10, 15);

    @TempDir
    Path dir;

    private ParkTestSupport t;

    @BeforeEach
    void setUp() {
        t = new ParkTestSupport(dir, LocalDate.of(2026, 10, 1));
        t.run(ParkTestSupport.CREATE_ANA, "TICKET CREATE;T001;ana@correo.com;2026-10-15",
                "GROUP CREATE;G01;Familia");
        t.addMembers("G01", 3);
        t.run("GROUPTICKET CREATE;TG01;G01;2026-10-15", "SYSTEM DATE;2026-10-15");
    }

    @Test
    void createAndRejectInvalidAttractions() {
        assertEquals("OK ATTRACTION CREATED", t.run("ATTRACTION CREATE;A01;Tornado;20"));
        Attraction a = t.park.getAttraction("A01");
        assertEquals("Tornado", a.getName());
        assertEquals(20, a.getCapacity());
        assertTrue(a.getPresent().isEmpty());
        assertTrue(t.run("ATTRACTION CREATE;A01;Otra;5").startsWith("ERROR"));
        assertTrue(t.run("ATTRACTION CREATE;A02;Otra;0").startsWith("ERROR"));
        assertTrue(t.run("ATTRACTION CREATE;A02;Otra;veinte").startsWith("ERROR"));
        assertTrue(t.run("ATTRACTION CREATE;A02; ;5").startsWith("ERROR"));
        assertTrue(t.run("ATTRACTION CREATE;A 2;Otra;5").startsWith("ERROR"));
    }

    @Test
    void individualAndGroupEntryThenExitAndStatus() {
        t.run("ATTRACTION CREATE;A01;Tornado;20");
        assertEquals("OK ATTRACTION ENTERED -> 1", t.run("ATTRACTION ENTER;A01;T001"));
        assertEquals("OK ATTRACTION ENTERED -> 3", t.run("ATTRACTION ENTER;A01;TG01"));
        String status = t.output("ATTRACTION STATUS;A01");
        assertTrue(status.contains("ATTRACTION A01 - Tornado"));
        assertTrue(status.contains("Capacity: 20"));
        assertTrue(status.contains("Present: 4"));
        assertTrue(status.contains("Available: 16"));
        assertTrue(status.contains("Ana Garcia Lopez (12345678Z)"));
        assertTrue(status.contains("U2 Test"));

        assertEquals("OK ATTRACTION EXITED -> 1", t.run("ATTRACTION EXIT;A01;T001"));
        status = t.output("ATTRACTION STATUS;A01");
        assertTrue(status.contains("Present: 3"));
        assertTrue(status.contains("Available: 17"));
        assertTrue(!status.contains("Ana Garcia Lopez"));
    }

    @Test
    void internalTicketCanBeUsedIndividually() {
        t.run("ATTRACTION CREATE;A01;Tornado;20");
        assertEquals("OK ATTRACTION ENTERED -> 1", t.run("ATTRACTION ENTER;A01;TG01-1"));
        assertTrue(t.run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertEquals("OK ATTRACTION EXITED -> 1", t.run("ATTRACTION EXIT;A01;TG01"));
    }

    @Test
    void ticketValidOnlyOnItsDate() {
        t.run("ATTRACTION CREATE;A01;Tornado;20", "SYSTEM DATE;2026-10-14");
        assertTrue(t.run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        t.run("SYSTEM DATE;2026-10-16");
        assertTrue(t.run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertTrue(t.park.getAttraction("A01").getPresent().isEmpty());
    }

    @Test
    void groupEntryIsAtomicAndCapacityNeverExceeded() {
        t.run("ATTRACTION CREATE;A01;Tornado;3", "ATTRACTION ENTER;A01;T001");
        assertTrue(t.run("ATTRACTION ENTER;A01;TG01").startsWith("ERROR"));
        assertEquals(1, t.park.getAttraction("A01").getPresent().size());
        t.run("ATTRACTION EXIT;A01;T001");
        assertEquals("OK ATTRACTION ENTERED -> 3", t.run("ATTRACTION ENTER;A01;TG01"));
        assertTrue(t.run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
        assertEquals(0, t.park.getAttraction("A01").getAvailable());
    }

    @Test
    void sameUsersCannotEnterTwice() {
        t.run("ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION ENTER;A01;T001");
        assertTrue(t.run("ATTRACTION ENTER;A01;T001").startsWith("ERROR"));
    }

    @Test
    void exitWithoutUsersInsideAndExitIgnoresDate() {
        t.run("ATTRACTION CREATE;A01;Tornado;20");
        assertTrue(t.run("ATTRACTION EXIT;A01;T001").startsWith("ERROR"));
        t.run("ATTRACTION ENTER;A01;T001", "SYSTEM DATE;2026-10-20");
        assertEquals("OK ATTRACTION EXITED -> 1", t.run("ATTRACTION EXIT;A01;T001"));
    }

    @Test
    void deleteOnlyEmptyAttractions() {
        t.run("ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION ENTER;A01;T001");
        assertTrue(t.run("ATTRACTION DELETE;A01").startsWith("ERROR"));
        t.run("ATTRACTION EXIT;A01;T001");
        assertEquals("OK ATTRACTION DELETED", t.run("ATTRACTION DELETE;A01"));
        assertTrue(t.run("ATTRACTION STATUS;A01").startsWith("ERROR"));
    }

    @Test
    void attractionsPersistAndStartEmpty() throws IOException {
        t.run("ATTRACTION CREATE;A01;Tornado;20", "ATTRACTION ENTER;A01;T001");
        assertEquals("A01;Tornado;20" + System.lineSeparator(), Files.readString(dir.resolve("attractions.txt")));
        Attraction restored = t.restart(DAY).park.getAttraction("A01");
        assertEquals("Tornado", restored.getName());
        assertEquals(20, restored.getCapacity());
        assertTrue(restored.getPresent().isEmpty());
        assertTrue(t.restart(DAY).output("ATTRACTION STATUS;A01").contains("(none)"));
    }
}
