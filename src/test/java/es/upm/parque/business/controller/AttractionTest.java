package es.upm.parque.business.controller;

import static es.upm.parque.TestContext.email;
import static es.upm.parque.TestContext.error;
import static es.upm.parque.TestContext.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;

import es.upm.parque.TestContext;
import es.upm.parque.business.model.Attraction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AttractionTest {

    @TempDir
    Path dir;

    private TestContext ctx;

    @BeforeEach
    void setUp() throws IOException {
        ctx = TestContext.start(dir);
        ok(ctx.park().setSystemDate("2026-10-15"));
    }

    private Attraction<?> attraction(String id) {
        return ctx.attractions().find(id);
    }

    @Test
    void createsAndDeletesAttractions() {
        ok(ctx.attractions().createAttraction("A01", "Tornado", "20", null));
        error(ctx.attractions().createAttraction("A01", "Otra", "5", null));
        error(ctx.attractions().createAttraction("A02", "Noria", "0", null));
        error(ctx.attractions().createAttraction("A02", "Noria", "x", null));
        error(ctx.attractions().createAttraction("A02", "", "5", null));
        assertEquals(20, attraction("A01").getCapacity());
        ok(ctx.attractions().deleteAttraction("A01"));
        error(ctx.attractions().deleteAttraction("A01"));
    }

    @Test
    void individualAndGroupTicketsEnterThroughTheSameLogic() {
        ok(ctx.attractions().createAttraction("A01", "Tornado", "20", null));
        ctx.user(9);
        ok(ctx.tickets().createTicket("T001", email(9), "2026-10-15"));
        ctx.group("G01", 3);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));

        assertEquals("OK ENTERED A01 -> 1 user(s)", ctx.attractions().enterAttraction("A01", "T001", null));
        assertEquals("OK ENTERED A01 -> 3 user(s)", ctx.attractions().enterAttraction("A01", "TG01", null));
        assertEquals(4, attraction("A01").getPresent().size());
        error(ctx.attractions().deleteAttraction("A01"));

        String status = ctx.attractions().attractionStatus("A01");
        assertTrue(status.contains("A01"));
        assertTrue(status.contains("Tornado"));
        assertTrue(status.contains("Maximum capacity: 20"));
        assertTrue(status.contains("Users present: 4"));
        assertTrue(status.contains("Available places: 16"));
        assertTrue(status.contains(email(9)) && status.contains(email(1)) && status.contains(email(3)));

        ok(ctx.attractions().exitAttraction("A01", "T001"));
        status = ctx.attractions().attractionStatus("A01");
        assertTrue(status.contains("Users present: 3"));
        assertTrue(status.contains("Available places: 17"));
        assertTrue(!status.contains(email(9)));
        error(ctx.attractions().exitAttraction("A01", "T001"));
    }

    @Test
    void onlyTicketsForTheSystemDateAreAccepted() {
        ok(ctx.attractions().createAttraction("A01", "Tornado", "20", null));
        ctx.user(1);
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-16"));
        error(ctx.attractions().enterAttraction("A01", "T001", null));
        ok(ctx.park().setSystemDate("2026-10-16"));
        ok(ctx.attractions().enterAttraction("A01", "T001", null));
        ok(ctx.park().setSystemDate("2026-10-20"));
        ok(ctx.attractions().exitAttraction("A01", "T001"));
    }

    @Test
    void capacityIsNeverExceededAndGroupEntryIsAtomic() {
        ok(ctx.attractions().createAttraction("A01", "Tornado", "4", null));
        ctx.user(9);
        ok(ctx.tickets().createTicket("T001", email(9), "2026-10-15"));
        ctx.group("G01", 4);
        ok(ctx.tickets().createGroupTicket("TG01", "G01", "2026-10-15"));
        ok(ctx.attractions().enterAttraction("A01", "T001", null));
        error(ctx.attractions().enterAttraction("A01", "TG01", null));
        assertEquals(1, attraction("A01").getPresent().size());
        error(ctx.attractions().enterAttraction("A01", "T001", null));
        assertEquals(1, attraction("A01").getPresent().size());
        ok(ctx.attractions().exitAttraction("A01", "T001"));
        ok(ctx.attractions().enterAttraction("A01", "TG01", null));
        assertEquals(0, attraction("A01").getAvailablePlaces());
    }

    @Test
    void reportsUnknownAttractionsAndTickets() {
        ok(ctx.attractions().createAttraction("A01", "Tornado", "4", null));
        error(ctx.attractions().enterAttraction("A99", "T001", null));
        error(ctx.attractions().enterAttraction("A01", "T999", null));
        error(ctx.attractions().exitAttraction("A01", "T999"));
        error(ctx.attractions().attractionStatus("A99"));
    }

    @Test
    void attractionsPersistButStartEmpty() throws IOException {
        ok(ctx.attractions().createAttraction("A01", "Tornado", "20", null));
        ctx.user(1);
        ok(ctx.tickets().createTicket("T001", email(1), "2026-10-15"));
        ok(ctx.attractions().enterAttraction("A01", "T001", null));
        Attraction<?> restored = ctx.restart().attractions().find("A01");
        assertEquals("Tornado", restored.getName());
        assertEquals(20, restored.getCapacity());
        assertTrue(restored.getPresent().isEmpty());
    }
}
