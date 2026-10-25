package es.upm.parque;

import static es.upm.parque.ParkTestSupport.email;
import static es.upm.parque.ParkTestSupport.error;
import static es.upm.parque.ParkTestSupport.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AttractionTest {

    @TempDir
    Path dir;

    private ParkController park;

    @BeforeEach
    void setUp() throws IOException {
        park = ParkTestSupport.park(dir);
        ok(park.setSystemDate("2026-10-15"));
    }

    private Attraction attraction(String id) {
        return park.getAttractionManager().find(id);
    }

    @Test
    void createsAndDeletesAttractions() {
        ok(park.createAttraction("A01", "Tornado", "20"));
        error(park.createAttraction("A01", "Otra", "5"));
        error(park.createAttraction("A02", "Noria", "0"));
        error(park.createAttraction("A02", "Noria", "x"));
        error(park.createAttraction("A02", "", "5"));
        assertEquals(20, attraction("A01").getCapacity());
        ok(park.deleteAttraction("A01"));
        error(park.deleteAttraction("A01"));
    }

    @Test
    void individualAndGroupTicketsEnterThroughTheSameLogic() {
        ok(park.createAttraction("A01", "Tornado", "20"));
        ParkTestSupport.user(park, 9);
        ok(park.createTicket("T001", email(9), "2026-10-15"));
        ParkTestSupport.group(park, "G01", 3);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));

        assertEquals("OK ENTERED A01 -> 1 user(s)", park.enterAttraction("A01", "T001"));
        assertEquals("OK ENTERED A01 -> 3 user(s)", park.enterAttraction("A01", "TG01"));
        assertEquals(4, attraction("A01").getPresent().size());
        error(park.deleteAttraction("A01"));

        String status = park.attractionStatus("A01");
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Tornado"));
        assertTrue(status.contains("Maximum capacity: 20"));
        assertTrue(status.contains("Users present: 4"));
        assertTrue(status.contains("Available places: 16"));
        assertTrue(status.contains(email(9)) && status.contains(email(1)) && status.contains(email(3)));

        ok(park.exitAttraction("A01", "T001"));
        status = park.attractionStatus("A01");
        assertTrue(status.contains("Users present: 3"));
        assertTrue(status.contains("Available places: 17"));
        assertTrue(!status.contains(email(9)));
        error(park.exitAttraction("A01", "T001"));
    }

    @Test
    void onlyTicketsForTheSystemDateAreAccepted() {
        ok(park.createAttraction("A01", "Tornado", "20"));
        ParkTestSupport.user(park, 1);
        ok(park.createTicket("T001", email(1), "2026-10-16"));
        error(park.enterAttraction("A01", "T001"));
        ok(park.setSystemDate("2026-10-16"));
        ok(park.enterAttraction("A01", "T001"));
        ok(park.setSystemDate("2026-10-20"));
        ok(park.exitAttraction("A01", "T001"));
    }

    @Test
    void capacityIsNeverExceededAndGroupEntryIsAtomic() {
        ok(park.createAttraction("A01", "Tornado", "4"));
        ParkTestSupport.user(park, 9);
        ok(park.createTicket("T001", email(9), "2026-10-15"));
        ParkTestSupport.group(park, "G01", 4);
        ok(park.createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(park.enterAttraction("A01", "T001"));
        error(park.enterAttraction("A01", "TG01"));
        assertEquals(1, attraction("A01").getPresent().size());
        error(park.enterAttraction("A01", "T001"));
        assertEquals(1, attraction("A01").getPresent().size());
        ok(park.exitAttraction("A01", "T001"));
        ok(park.enterAttraction("A01", "TG01"));
        assertEquals(0, attraction("A01").getAvailablePlaces());
    }

    @Test
    void reportsUnknownAttractionsAndTickets() {
        ok(park.createAttraction("A01", "Tornado", "4"));
        error(park.enterAttraction("A99", "T001"));
        error(park.enterAttraction("A01", "T999"));
        error(park.exitAttraction("A01", "T999"));
        error(park.attractionStatus("A99"));
    }

    @Test
    void attractionsPersistButStartEmpty() throws IOException {
        ok(park.createAttraction("A01", "Tornado", "20"));
        ParkTestSupport.user(park, 1);
        ok(park.createTicket("T001", email(1), "2026-10-15"));
        ok(park.enterAttraction("A01", "T001"));
        Attraction restored = ParkTestSupport.restart(dir).getAttractionManager().find("A01");
        assertEquals("Tornado", restored.getName());
        assertEquals(20, restored.getCapacity());
        assertTrue(restored.getPresent().isEmpty());
    }
}
