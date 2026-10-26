package es.upm.parque.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.upm.parque.TestPark;
import es.upm.parque.business.ParkException;
import es.upm.parque.business.model.Group;
import es.upm.parque.business.model.GroupTicket;
import es.upm.parque.business.model.IndividualTicket;
import es.upm.parque.business.model.User;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TicketControllerTest {

    @TempDir
    Path dir;

    private TestPark park;
    private User ana;

    @BeforeEach
    void setUp() {
        park = new TestPark(dir);
        park.users.create("Ana", "Garcia", "12345678Z", "ana@correo.com", "Clave1!", "6", "1234567890123456");
        ana = park.users.findByEmail("ana@correo.com");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 1));
    }

    @Test
    void createChargesCurrentPriceAndRefersToUser() {
        IndividualTicket t = park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
        assertSame(ana, t.getUser());
        assertEquals(new BigDecimal("40.00"), t.getPrice());

        park.controller.setTicketPrice(new BigDecimal("60.00"));
        assertEquals(new BigDecimal("60.00"), park.tickets.create("T002", "ana@correo.com", "2026-10-01").getPrice());
        assertEquals(new BigDecimal("40.00"), park.tickets.findById("T001").getPrice());
    }

    @Test
    void createRejections() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertThrows(ParkException.class, () -> park.tickets.create("T001", "ana@correo.com", "2026-10-20"));
        assertThrows(ParkException.class, () -> park.tickets.create("T002", "nadie@correo.com", "2026-10-15"));
        assertThrows(ParkException.class, () -> park.tickets.create(" ", "ana@correo.com", "2026-10-15"));
        assertThrows(ParkException.class, () -> park.tickets.create("T003", "ana@correo.com", "2026-09-30"));
        assertThrows(ParkException.class, () -> park.tickets.create("T004", "ana@correo.com", "15/10/2026"));
        assertEquals(1, park.tickets.getTickets().size());
    }

    @Test
    void deleteOnlyStrictlyFutureTickets() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertThrows(ParkException.class, () -> park.tickets.delete("T001"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 16));
        assertThrows(ParkException.class, () -> park.tickets.delete("T001"));
        assertNotNull(park.tickets.findById("T001"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 14));
        park.tickets.delete("T001");
        assertNull(park.tickets.findById("T001"));
        assertThrows(ParkException.class, () -> park.tickets.delete("T001"));
    }

    @Test
    void currentOrFutureTicketDetection() {
        assertFalse(park.tickets.hasCurrentOrFutureTicket(ana));
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        assertTrue(park.tickets.hasCurrentOrFutureTicket(ana));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertTrue(park.tickets.hasCurrentOrFutureTicket(ana));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 16));
        assertFalse(park.tickets.hasCurrentOrFutureTicket(ana));
        assertNotNull(park.tickets.findById("T001"));
    }

    @Test
    void removeTicketsOfUser() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.tickets.removeTicketsOf(ana);
        park.tickets.removeTicketsOf(ana);
        assertTrue(new TestPark(dir).tickets.getTickets().isEmpty());
    }

    @Test
    void ticketsSurviveRestartWithTheirPrice() {
        park.controller.setTicketPrice(new BigDecimal("60.00"));
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        TestPark restarted = new TestPark(dir);
        IndividualTicket t = (IndividualTicket) restarted.tickets.findById("T001");
        assertEquals(new BigDecimal("60.00"), t.getPrice());
        assertSame(restarted.users.findByEmail("ana@correo.com"), t.getUser());
        assertEquals(MainController.DEFAULT_TICKET_PRICE, restarted.controller.getTicketPrice());
    }

    private void createMembers(int count) {
        park.groups.create("G01", "Familia");
        park.groups.addMember("G01", "ana@correo.com");
        for (int i = 1; i < count; i++) {
            String dni = String.format("%08d", i);
            dni = dni + "TRWAGMYFPDXBNJZSQVHLCKE".charAt(i % 23);
            park.users.create("U" + i, "X", dni, "u" + i + "@correo.com", "Clave1!", "6", "1234567890123456");
            park.groups.addMember("G01", "u" + i + "@correo.com");
        }
    }

    @Test
    void groupTicketGeneratesOneTicketPerMember() {
        createMembers(3);
        GroupTicket gt = park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertEquals(new BigDecimal("108.00"), gt.getPrice());
        assertEquals(3, gt.getTickets().size());
        assertEquals(park.groups.findById("G01").getMembers(), gt.getUsers());
        for (IndividualTicket t : gt.getTickets()) {
            assertEquals(LocalDate.of(2026, 10, 15), t.getDate());
            assertEquals(new BigDecimal("40.00"), t.getPrice());
            assertSame(t, park.tickets.findById(t.getId()));
        }
        assertEquals("TG01-1", gt.getTickets().get(0).getId());
        assertTrue(park.tickets.hasCurrentOrFutureTicket(ana));
    }

    @Test
    void groupTicketRejections() {
        park.groups.create("G00", "Vacio");
        assertThrows(ParkException.class, () -> park.tickets.createGroupTicket("TG01", "G00", "2026-10-15"));
        assertThrows(ParkException.class, () -> park.tickets.createGroupTicket("TG01", "G99", "2026-10-15"));
        createMembers(2);
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        park.tickets.create("TG02-2", "ana@correo.com", "2026-10-15");
        assertThrows(ParkException.class, () -> park.tickets.createGroupTicket("T001", "G01", "2026-10-15"));
        assertThrows(ParkException.class, () -> park.tickets.createGroupTicket("TG02", "G01", "2026-10-15"));
        assertThrows(ParkException.class, () -> park.tickets.createGroupTicket("TG03", "G01", "2026-09-30"));
        assertTrue(park.tickets.getGroupTickets().isEmpty());
        assertEquals(2, park.tickets.getIndividualTickets().size());
        park.tickets.createGroupTicket("TG04", "G01", "2026-10-15");
        assertThrows(ParkException.class, () -> park.tickets.create("TG04", "ana@correo.com", "2026-10-15"));
    }

    @Test
    void groupTicketDeletionAndInternalTickets() {
        createMembers(2);
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertThrows(ParkException.class, () -> park.tickets.delete("TG01-1"));
        assertThrows(ParkException.class, () -> park.tickets.delete("TG01"));
        assertThrows(ParkException.class, () -> park.tickets.deleteGroupTicket("T999"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertThrows(ParkException.class, () -> park.tickets.deleteGroupTicket("TG01"));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 14));
        park.tickets.deleteGroupTicket("TG01");
        assertTrue(park.tickets.getTickets().isEmpty());
    }

    @Test
    void groupLockFollowsSystemDate() {
        createMembers(2);
        Group group = park.groups.findById("G01");
        assertFalse(park.tickets.isGroupLocked(group));
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        assertTrue(park.tickets.isGroupLocked(group));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 15));
        assertTrue(park.tickets.isGroupLocked(group));
        park.controller.setCurrentDate(LocalDate.of(2026, 10, 16));
        assertFalse(park.tickets.isGroupLocked(group));
    }

    @Test
    void removingUserAndGroupCleansCompositions() {
        createMembers(2);
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        park.tickets.removeTicketsOf(ana);
        GroupTicket gt = (GroupTicket) park.tickets.findById("TG01");
        assertEquals(1, gt.getTickets().size());
        assertNull(park.tickets.findById("TG01-1"));
        park.tickets.removeGroupTicketsOf(park.groups.findById("G01"));
        park.tickets.removeGroupTicketsOf(park.groups.findById("G01"));
        assertTrue(new TestPark(dir).tickets.getTickets().isEmpty());
    }

    @Test
    void groupTicketsSurviveRestart() {
        createMembers(3);
        park.tickets.createGroupTicket("TG01", "G01", "2026-10-15");
        TestPark restarted = new TestPark(dir);
        GroupTicket gt = (GroupTicket) restarted.tickets.findById("TG01");
        assertSame(restarted.groups.findById("G01"), gt.getGroup());
        assertEquals(new BigDecimal("108.00"), gt.getPrice());
        assertEquals(restarted.groups.findById("G01").getMembers(), gt.getUsers());
        assertEquals(4, restarted.tickets.getTickets().size());
    }

    @Test
    void saveFailureRestoresPersistedState() {
        park.tickets.create("T001", "ana@correo.com", "2026-10-15");
        Path file = dir.resolve("tickets.txt");
        assertTrue(file.toFile().setWritable(false));
        try {
            assertThrows(ParkException.class, () -> park.tickets.create("T002", "ana@correo.com", "2026-10-15"));
            assertNull(park.tickets.findById("T002"));
        } finally {
            assertTrue(file.toFile().setWritable(true));
        }
    }
}
