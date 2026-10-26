package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestPark;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.AttractionType;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Delivery 3 persistence: cascading deletions of passes, atomicity and restart of the new state.
 */
class TicketDeletionCascadeTest {

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
        park.passes.buySpeedPasses("T001", "10");
        park.passes.buySpecialPass("T001");
        park.groups.create("G01", "F");
        park.groups.addMember("G01", "ana@correo.com");
        park.groups.addMember("G01", "pedro@correo.com");
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-20");
        park.passes.buySpeedPasses("TG01-1", "1");
        park.passes.buySpecialPass("TG01-2");
    }

    private int storedPasses() throws IOException {
        return Files.readAllLines(dir.resolve("passes.txt")).size();
    }

    @Test
    void ticketDeletionRemovesItsPasses() throws IOException {
        assertEquals(13, storedPasses());
        park.tickets.delete("T001");
        assertNull(park.passes.findPass("SP1"));
        assertNull(park.passes.findPass("XP1"));
        assertEquals(2, storedPasses());
        assertNull(new TestPark(dir).passes.findPass("SP5"));
    }

    @Test
    void groupTicketDeletionRemovesPassesThenTicketsThenGroupTicket() throws IOException {
        IndividualTicket internal = park.tickets.findIndividualTicket("TG01-1");
        park.tickets.deleteGroupTicket("TG01");
        assertTrue(internal.getPasses().isEmpty());
        assertNull(park.tickets.findById("TG01-1"));
        assertNull(park.tickets.findById("TG01-2"));
        assertNull(park.tickets.findById("TG01"));
        assertEquals(11, storedPasses());
        TestPark restarted = new TestPark(dir);
        assertNull(restarted.passes.findPass("SP11"));
        assertNull(restarted.tickets.findById("TG01"));
        assertNotNull(restarted.passes.findPass("SP1"));
    }

    @Test
    void rejectedDeletionsKeepEverything() throws IOException {
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 20));
        assertThrows(ParkException.class, () -> park.tickets.deleteGroupTicket("TG01"));
        assertThrows(ParkException.class, () -> park.tickets.delete("TG01-1"));
        assertEquals(13, storedPasses());
        assertEquals(1, park.tickets.findIndividualTicket("TG01-1").getPasses().size());
    }

    @Test
    void storageFailureLeavesNoPartialDeletion() throws IOException {
        Path tickets = dir.resolve("tickets.txt");
        assertTrue(tickets.toFile().setWritable(false));
        try {
            assertThrows(ParkException.class, () -> park.tickets.deleteGroupTicket("TG01"));
            assertThrows(ParkException.class, () -> park.tickets.delete("T001"));
        } finally {
            assertTrue(tickets.toFile().setWritable(true));
        }
        GroupTicket group = (GroupTicket) park.tickets.findById("TG01");
        assertEquals(2, group.getTickets().size());
        assertEquals(1, park.tickets.findIndividualTicket("TG01-1").getPasses().size());
        assertEquals(11, park.tickets.findIndividualTicket("T001").getPasses().size());
        assertEquals(13, storedPasses());
        TestPark restarted = new TestPark(dir);
        assertEquals(2, ((GroupTicket) restarted.tickets.findById("TG01")).getTickets().size());
        assertNotNull(restarted.passes.findPass("XP2"));
    }

    @Test
    void consumedPassesOfInternalTicketsAndAttractionTypesSurviveRestart() {
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 20));
        for (AttractionType type : AttractionType.values()) {
            park.attractions.create("A" + type.ordinal(), type.name(), "10", type.name());
        }
        park.attractions.enter("A2", "TG01", false);
        park.attractions.exit("A2", "TG01");
        park.attractions.enter("A3", "TG01-2", false);

        TestPark restarted = new TestPark(dir);
        assertTrue(restarted.passes.findPass("XP2").isConsumed());
        assertEquals("TG01-2", restarted.passes.ticketOf("XP2").getId());
        assertEquals(new BigDecimal("40.00"), restarted.passes.findPass("XP2").getPrice());
        for (AttractionType type : AttractionType.values()) {
            assertEquals(type, restarted.attractions.findById("A" + type.ordinal()).getType());
        }
    }
}
